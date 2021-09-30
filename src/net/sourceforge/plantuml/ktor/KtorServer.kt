package net.sourceforge.plantuml.ktor

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.code.TranscoderUtil
import java.io.ByteArrayOutputStream


private fun getImageFormat(format: String?) : FileFormat {
    return if (format == null) FileFormat.DEBUG else when(format) {
        "png" -> FileFormat.PNG
        "svg" -> FileFormat.SVG
        else -> FileFormat.DEBUG
    }
}

private fun getImageContentType(format: FileFormat) : ContentType {
    return when(format) {
        FileFormat.SVG -> ContentType.Image.SVG
        FileFormat.PNG -> ContentType.Image.PNG
        else -> ContentType.Any
    }
}

fun serve() {
    embeddedServer(Netty, port = 8080)  {

        routing {
            get("/plantuml/{format}/{compressed}") {

                val format = getImageFormat(this.call.parameters["format"])
                if (format == FileFormat.DEBUG) {
                    call.respond(HttpStatusCode.BadRequest, "format is not supported")
                    return@get
                }
                var compressed = this.call.parameters["compressed"]
                if (compressed.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "content is required")
                    return@get
                }
                if (!compressed.startsWith("~")) {
                    compressed = "~1$compressed"
                }
                val transcoder = TranscoderUtil.getDefaultTranscoderProtected()

                val source = try { transcoder.decode(compressed) } catch (ignored: Throwable) {
                    call.respond(HttpStatusCode.InternalServerError, ignored.message ?: "")
                    null
                } ?: return@get

                val ssr = SourceStringReader(source)

                val blocks = ssr.blocks

                if (blocks.size > 0) {
                    val fileFormatOption = FileFormatOption(format)
                    val system = blocks[0].diagram

                    val os = ByteArrayOutputStream()

                    val image = system.exportDiagram(os, 0, fileFormatOption)
                    os.close()

                    call.response.headers.append("X-PlantUML-Diagram-Width", image.width.toString())
                    call.response.headers.append("X-PlantUML-Diagram-Height", image.height.toString())

                    call.respondBytes(getImageContentType(format), HttpStatusCode.OK, suspend {
                        os.toByteArray()
                    })
                }

            }
        }
    }.start(true)
}
