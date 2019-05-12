package com.teikoku

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage


class SceneBuilder  {
    companion object {
        fun scene(root:Parent):Scene {

            val scene = Scene(root)
            scene.stylesheets.add(this::class.java.getResource("style.css").toExternalForm())
            return scene
        }
    }



}

class MainApp2 : Application() {

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("scene.fxml"))

        val scene = SceneBuilder.scene(root)
        stage.title = "JavaFX and Gradle"
        stage.scene = scene
        stage.show()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(MainApp2::class.java, *args)
        }
    }

}