package com.teikoku

import javafx.event.ActionEvent
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



class Layer(val str: String) {
    companion object {
        var counter = 0
    }

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
    private lateinit var tree: TreeView<Layer>
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

    fun createLayer(str: String): Layer {
        return Layer(str)
    }

    fun createMenu(): ContextMenu {
        return ContextMenu().apply {
            val addMenuItem = MenuItem("Add Employee")
            items.add(addMenuItem)
            addMenuItem.setOnAction(this@FXMLController2::addLayer)
        }
    }

    fun addLayer(ae: ActionEvent) {
        val l = createLayer("aaaa")
        val newEmployee = TreeItem(l)
        tree.selectionModel.selectedItem.children.add(newEmployee)
        boxPane.children.add(l.box)
        makeDraggable(l.box)

    }

    override fun initialize(url: URL?, rb: ResourceBundle?) {
        selectbox.isVisible = false
        val menu = createMenu()

        tree.root = TreeItem(Layer("root"), null)
        tree.setCellFactory { TextFieldTreeCellImpl(menu) }


        selectbox.isMouseTransparent = true
        makeDraggable(selectbox)

        filebtn.setOnAction(this::onSetImg)
        accordion.expandedPane=pane1
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


    private inner class TextFieldTreeCellImpl(val menu: ContextMenu) : TreeCell<Layer>() {


        override fun startEdit() {
            super.startEdit()

            text = null
            graphic = null
        }

        override fun cancelEdit() {
            super.cancelEdit()

            text = item.str
            graphic = treeItem.graphic
        }

        override fun updateItem(item: Layer?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty) {
                text = null
                graphic = null
            } else {
                text = item!!.str
                graphic = treeItem.graphic
                if (!treeItem.isLeaf && treeItem.parent != null) {
                }
            }
            contextMenu = menu
        }

    }

}