import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.nio.channels.Channels
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import kotlin.io.path.Path
import kotlinx.coroutines.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

//Java console color constants
const val TEXT_RED    : String = "\u001B[31m"
const val TEXT_GREEN  : String = "\u001B[32m"
const val TEXT_CYAN   : String = "\u001B[36m"
const val TEXT_YELLOW : String = "\u001B[33m"

const val domain = "https://mordbeitisch.de"
val gamesDirectory = Path("/home/rechenwerk/Desktop/Mord bei Tisch")

@OptIn(ExperimentalPathApi::class)
suspend fun main() = coroutineScope {
    val games = readGames()
    gamesDirectory.deleteRecursively()
    Files.createDirectory(gamesDirectory)
    games.forEach { launch { download(it) } }
}

suspend fun download(game: Game) = coroutineScope {
    println("${TEXT_RED}Lade ${game.name} herunter.")
    val gameDirectory = Files.createDirectory(gamesDirectory.resolve(game.name))
    val hinweiseDirectory = Files.createDirectory(gameDirectory.resolve("Hinweise"))
    val rollenhefteDirectory = Files.createDirectory(gameDirectory.resolve("Rollenhefte"))
    launch {
        game.gastgeberheft.downloadTo(gameDirectory.resolve("Gastgeberheft").toString(), game.password)
        println("$TEXT_CYAN${game.name}'s Gastgeberheft heruntergeladen.")
    }
    game.hinweise.forEach{ hinweis ->
        launch {
            hinweis.src.downloadTo(hinweiseDirectory.resolve(hinweis.name).toString(), game.password)
            println("$TEXT_YELLOW${game.name}'s ${hinweis.name} heruntergeladen.")
        }
    }
    game.rollenhefte.forEach{ rollenheft ->
        launch {
            rollenheft.src.downloadTo(rollenhefteDirectory.resolve(rollenheft.name).toString(), game.password)
            println("$TEXT_GREEN${game.name}'s ${rollenheft.name} heruntergeladen.")
        }
    }
}

suspend fun String.downloadTo(name: String, password: String) = coroutineScope {
    FileOutputStream("$name.pdf").use { output ->
        Channels.newChannel(URL("$domain${this@downloadTo}").openStream()).use { input ->
            output.channel.transferFrom(input, 0, Long.MAX_VALUE)
            val file = File("$name.pdf")
            PDDocument.load(file, password).use{ pdf ->
                pdf.isAllSecurityToBeRemoved = true
                pdf.save(file)
            }

        }
    }
}

fun readGames(): List<Game> = ObjectMapper().registerModule(KotlinModule()).readValue<List<Game>>(ClassLoader.getSystemClassLoader().getResourceAsStream("resources.json")!!)

data class Game(
    val name: String,
    val password: String,
    val gastgeberheft: String,
    val rollenhefte: List<Material>,
    val hinweise: List<Material>
)

data class Material(
    val name: String,
    val src: String
)