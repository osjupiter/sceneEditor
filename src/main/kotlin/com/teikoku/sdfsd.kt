package com.teikoku

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import java.net.URL
import java.util.*
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import java.io.File
import javafx.stage.FileChooser
import javafx.scene.control.TitledPane
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.shape.Circle


class Component( val name: String) {
    companion object {
        var counter = 0
    }

    var layer=false
    val id = counter++
    val box = Rectangle().apply {
        this.width = 300.0
        height = 200.0
        fill = Paint.valueOf("FF0")
        
    }
    var image: ImageView? = null

    fun hasNode(node: Node): Boolean {
        return box === node || image === node
    }

}

class FXMLController2 : Initializable {

    var nowselect: Node? = null

    @FXML
    private lateinit var tree: TreeView<Component>
    @FXML
    private lateinit var boxPane: AnchorPane
    @FXML
    private lateinit var selectbox: Rectangle
    @FXML
    private lateinit var filebtn: Button
    @FXML
    private lateinit var pane1: TitledPane
    @FXML
    private lateinit var accordion: Accordion
    @FXML
    private lateinit var circle:Circle
    @FXML
    private lateinit var mypane:Pane

    override fun initialize(url: URL?, rb: ResourceBundle?) {
        selectbox.isVisible = false
        val menu = createMenu()

        val root=Component("root")
        root.layer=true
        tree.root = TreeItem(root, null)
        tree.setCellFactory { TextFieldTreeCellImpl(menu) }


        selectbox.isMouseTransparent = true
        mypane.isMouseTransparent=true
        makeDraggable(selectbox)

        filebtn.setOnAction(this::onSetImg)
        accordion.expandedPane=pane1
    }

    fun createLayer(str: String): Component {
        return Component(str)
    }

    fun createMenu(): ContextMenu {
        return ContextMenu().apply {
            val addMenuItem = MenuItem("Add Employee")
            items.add(addMenuItem)
            addMenuItem.setOnAction(this@FXMLController2::addLayer)
            val group = MenuItem("Add group")
            items.add(group)
            group.setOnAction(this@FXMLController2::addGroup)
        }
    }

    fun addLayer(ae: ActionEvent) {
        val l = createLayer("aaaa")
        val newEmployee = TreeItem(l)
        tree.selectionModel.selectedItem.children.add(newEmployee)
        boxPane.children.add(l.box)
        makeDraggable(l.box)
    }
    fun addGroup(ae: ActionEvent) {
        val l = Component("bbb")
        l.layer=true
        val newEmployee = TreeItem(l)
        tree.selectionModel.selectedItem.children.add(newEmployee)
    }


    internal class Delta {
        var x: Double = 0.toDouble()
        var y: Double = 0.toDouble()
    }

    fun selectFile(): File? {
        val fc = FileChooser()
        fc.title = "ファイル選択"
        fc.extensionFilters.addAll(
                FileChooser.ExtensionFilter("イメージファイル", "*.jpg", "*.png")
        )
        // 初期フォルダをホームに
        fc.initialDirectory = File(System.getProperty("user.home"))
        // ファイルダイアログ表示
        return fc.showOpenDialog(boxPane.scene.window)

    }

    fun onSetImg(ae: ActionEvent) {
        print(nowselect)

        if (nowselect == null) return
        val selected = findTree(tree.root) { it.value.hasNode(nowselect!!) } ?: return
        val file = selectFile() ?: return

        println(file.path)
        // 既存のノードを取り除く
        var origin:Node
        if (selected.value.image == null) {
            origin=selected.value.box
            boxPane.children.remove(selected.value.box)
        } else {
            origin=selected.value.image!!
            boxPane.children.remove(selected.value.image)
        }

        // 新ファイル
        val i=Image(file.inputStream())
        val img = ImageView(i).apply {
            layoutX = origin.layoutX
            layoutY = origin.layoutY
            fitWidth=i.width
            fitHeight=i.height
        }

        makeDraggable(img)
        //DragResizer.makeResizable(img)
        selected.value.image = img
        boxPane.children.add(selected.value.image)

    }

    fun <T> findTree(root: TreeItem<T>, match: (item: TreeItem<T>) -> Boolean): TreeItem<T>? {
        if (match(root)) {
            return root
        }
        root.children.forEach {
            val ret = findTree(it, match)
            if (ret != null) {
                return ret
            }
        }
        return null
    }

    fun setSelected(node: Node) {
        val w = if (node is Rectangle) node.width else if (node is ImageView) node.boundsInParent.width else 0.0
        val h = if (node is Rectangle) node.height else if (node is ImageView) node.boundsInParent.height else 0.0

        nowselect = node
        selectbox.isVisible = true
        selectbox.layoutX = node.layoutX
        selectbox.layoutY = node.layoutY
        selectbox.width = w
        selectbox.height = h
        selectbox.toFront()

        // circle
        circle.centerX=node.layoutX+selectbox.width
        circle.centerY=node.layoutY+selectbox.height

        circle.toFront()
        mypane.toFront()
        findTree(tree.root) { it.value.hasNode(nowselect!!) }?.let {
            tree.selectionModel.select(it)
        }
    }

    /** makes a stage draggable using a given node  */
    fun makeDraggable(byNode: Node) {
        val dragDelta = Delta()
        byNode.setOnMousePressed { mouseEvent ->
            // record a delta distance for the drag and drop operation.
            dragDelta.x = byNode.layoutX - mouseEvent.screenX
            dragDelta.y = byNode.layoutY - mouseEvent.screenY
            byNode.cursor = Cursor.MOVE
            setSelected(byNode)
        }
        byNode.setOnMouseReleased { byNode.cursor = Cursor.HAND }
        byNode.setOnMouseDragged { mouseEvent ->
            byNode.layoutX = mouseEvent.screenX + dragDelta.x
            byNode.layoutY = mouseEvent.screenY + dragDelta.y
            setSelected(byNode)
        }
        byNode.setOnMouseEntered { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown) {
                byNode.cursor = Cursor.HAND
            }
        }
        byNode.setOnMouseExited { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown) {
                byNode.cursor = Cursor.DEFAULT
            }
        }
    }


    private inner class TextFieldTreeCellImpl(val menu: ContextMenu) : TreeCell<Component>() {


        override fun startEdit() {
            super.startEdit()

            text = null
            graphic = null
        }

        override fun cancelEdit() {
            super.cancelEdit()

            text = item.name
            graphic = treeItem.graphic
        }

        override fun updateItem(item: Component?, empty: Boolean) {
            super.updateItem(item, empty)

            contextMenu = null
            if (empty) {
                text = null
                graphic = null
            } else {
                text = item!!.name
                graphic = treeItem.graphic
                if(item.layer){
                    contextMenu = menu
                }
            }
        }

    }

}
class DragResizer private constructor(private val region: Node) {

    private var y: Double = 0.toDouble()

    private var initMinHeight: Boolean = false

    private var dragging: Boolean = false

    protected fun mouseReleased(event: MouseEvent) {
        dragging = false
        region.cursor = Cursor.DEFAULT
    }

    protected fun mouseOver(event: MouseEvent) {
        if (isInDraggableZone(event) || dragging) {
            region.cursor = Cursor.S_RESIZE
        } else {
            region.cursor = Cursor.DEFAULT
        }
    }

    protected fun isInDraggableZone(event: MouseEvent): Boolean {
        return event.y > (region as ImageView).fitHeight - RESIZE_MARGIN
    }

    protected fun mouseDragged(event: MouseEvent) {
        if (!dragging) {
            return
        }

        val mousey = event.y

        val newHeight = (region as ImageView).minHeight(0.0) + (mousey - y)

        //region.(newHeight)
        (region as ImageView).fitHeight+=mousey-y

        y = mousey
    }

    protected fun mousePressed(event: MouseEvent) {

        // ignore clicks outside of the draggable margin
        if (!isInDraggableZone(event)) {
            return
        }

        dragging = true

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        //if (!initMinHeight) {
        //    region.setMinHeight(region.getHeight())
        //    initMinHeight = true
        //}

        y = event.getY()
    }

    companion object {

        /**
         * The margin around the control that a user can click in to start resizing
         * the region.
         */
        private val RESIZE_MARGIN = 5

        fun makeResizable(region: ImageView) {
            val resizer = DragResizer(region)

            region.onMousePressed = EventHandler<MouseEvent> { event -> resizer.mousePressed(event) }
            region.onMouseDragged = EventHandler<MouseEvent> { event -> resizer.mouseDragged(event) }
            region.setOnMouseMoved { event -> resizer.mouseOver(event) }
            region.setOnMouseReleased { event -> resizer.mouseReleased(event) }
        }
    }
}