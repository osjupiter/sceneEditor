package com.teikoku

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import java.net.URL
import java.util.*
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent


class FXMLController2 : Initializable {

    @FXML
    private lateinit var tree: TreeView<String>
    @FXML
    private lateinit var treeRoot: AnchorPane
    @FXML
    private lateinit var box: Node

    override fun initialize(url: URL?, rb: ResourceBundle?) {
        tree.root = TreeItem("aiueo", null)
        val list = listOf(
                TreeItem("aaa"),
                TreeItem("bbb"),
                TreeItem("caa"),
                TreeItem("ddd")
        )
        tree.root.children.addAll(list)
        tree.setCellFactory { TextFieldTreeCellImpl() }
        makeDraggable(box)
        val menu = ContextMenu().apply {
            val addMenuItem = MenuItem("Add Employee")
            items.add(addMenuItem)
            addMenuItem.setOnAction {
                val item = tree.selectionModel.selectedItem
                item.children.add(TreeItem("New Employee"))
            }
        }
        tree.contextMenu = menu
    }

    internal class Delta {
        var x: Double = 0.toDouble()
        var y: Double = 0.toDouble()
    }

    /** makes a stage draggable using a given node  */
    fun makeDraggable(byNode: Node) {
        val dragDelta = Delta()
        byNode.setOnMousePressed { mouseEvent ->
            // record a delta distance for the drag and drop operation.
            dragDelta.x = byNode.layoutX - mouseEvent.screenX
            dragDelta.y = byNode.layoutY - mouseEvent.screenY
            byNode.cursor = Cursor.MOVE
        }
        byNode.setOnMouseReleased { byNode.cursor = Cursor.HAND }
        byNode.setOnMouseDragged { mouseEvent ->
            byNode.layoutX = mouseEvent.screenX + dragDelta.x
            byNode.layoutY = mouseEvent.screenY + dragDelta.y
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


    private inner class TextFieldTreeCellImpl : TreeCell<String?>() {

        private lateinit var  menu:ContextMenu

        private val string: String
            get() = if (item == null) "" else item.toString()

        init {
             menu = ContextMenu().apply {
                val addMenuItem = MenuItem("Add Employee")
                items.add(addMenuItem)
                addMenuItem.setOnAction {
                    val newEmployee = TreeItem("New Employee")
                    treeItem.children.add(newEmployee)
                }
            }
        }


        override fun startEdit() {
            super.startEdit()

            setText(null)
            setGraphic(null)
        }

        override fun cancelEdit() {
            super.cancelEdit()

            setText(getItem() as String)
            setGraphic(getTreeItem().getGraphic())
        }

        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty) {
                setText(null)
                setGraphic(null)
            } else {
                setText(string)
                setGraphic(getTreeItem().getGraphic())
                if (!getTreeItem().isLeaf() && getTreeItem().getParent() != null) {
                    setContextMenu(menu)
                }
            }
        }

    }

}