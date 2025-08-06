package com.github.byronjl2003.intellijplugindemo.toolWindow

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.swing.*

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(project), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        fun getContent(project: Project): JBPanel<*> {
            val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())

            // Center Panel: Chat History
            val chatPanel = JTextPane()
            mainPanel.add(JScrollPane(chatPanel), BorderLayout.CENTER)

            // South Panel: Prompt Input
            val southPanel = JPanel(BorderLayout())
            val promptField = JBTextField()
            val sendButton = JButton("Send")
            southPanel.add(promptField, BorderLayout.CENTER)
            southPanel.add(sendButton, BorderLayout.EAST)
            mainPanel.add(southPanel, BorderLayout.SOUTH)

            // East Panel: File Selection and Dropdown
            val eastPanel = JBPanel<JBPanel<*>>(BorderLayout())
            val fileListModel = DefaultListModel<String>()
            val fileList = JBList(fileListModel)
            val fileButton = JButton("Add Files")
            val options = arrayOf("Option 1", "Option 2", "REST Call")
            val dropdown = JComboBox(options)

            fileButton.addActionListener {
                val descriptor = FileChooserDescriptor(true, true, true, true, false, true)
                FileChooser.chooseFiles(descriptor, project, null) { files ->
                    for (file in files) {
                        fileListModel.addElement(file.name)
                    }
                }
            }

            sendButton.addActionListener {
                if (dropdown.selectedItem == "REST Call") {
                    val client = HttpClient.newBuilder().build()
                    val request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts/1"))
                        .build()

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply { response ->
                            SwingUtilities.invokeLater {
                                chatPanel.text += "Response from API: \n${response.body()}\n"
                            }
                        }
                } else {
                    chatPanel.text += "User: ${promptField.text}\n"
                    promptField.text = ""
                }
            }

            val eastSouthPanel = JPanel(BorderLayout())
            eastSouthPanel.add(fileButton, BorderLayout.NORTH)
            eastSouthPanel.add(dropdown, BorderLayout.SOUTH)

            eastPanel.add(JScrollPane(fileList), BorderLayout.CENTER)
            eastPanel.add(eastSouthPanel, BorderLayout.SOUTH)
            mainPanel.add(eastPanel, BorderLayout.EAST)

            return mainPanel
        }
    }
}
