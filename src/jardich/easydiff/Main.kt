package jardich.easydiff

import com.sun.xml.internal.fastinfoset.util.StringArray
import java.io.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class Main {
    fun main(args: Array<String>) {
        println("Hello")
    }
}

fun main(args: Array<String>) {
    val dir = "./tmp/${System.currentTimeMillis()}/"
    clone("https://github.com/adrijardi/create-your-pizza.git", dir)

    val commits = commitLog("eea4f1046a33149844f7a200e6c3f6beccd5f01e", "b3b20e5357dc17d1e6ec33882814c2ba2530a49a", dir)

    commits.forEach { println(it) }

    val regRes = applyRegex(commits, Pattern.compile("\\d+"))

    println()
    regRes.forEach { println(it) }
}

fun clone(repo: String, tempDir: String) {
    val proc = Runtime.getRuntime().exec("git clone $repo $tempDir")

    pipeStreams(proc)
}

fun commitLog(fromCommit: String, toCommit: String, dir: String): Array<String> {
    val proc = Runtime.getRuntime().exec("git log --oneline $fromCommit..$toCommit", emptyArray<String>(), File(dir))

    pipeStream(proc.errorStream, System.err)

    return readStream(proc.inputStream)
}

fun applyRegex(input: Array<String>, regex: Pattern): Array<String> {
    return input.map { regex.matcher(it) }.filter { it.groupCount() > -1 }.map { it.group(0) }.toTypedArray()
}

fun pipeStreams(proc: Process) {
    pipeStream(proc.inputStream, System.out)
    pipeStream(proc.errorStream, System.err)
}

fun pipeStream(from: InputStream, to: OutputStream) {
    val buffer = ByteArray(1000)

    var len = from.read(buffer)
    while(len != -1) {
        to.write(buffer)
        len = from.read(buffer)
    }
}

fun readStream(s: InputStream): List<String> {
    val read = BufferedReader(InputStreamReader(s))
    val out = listOf<String>()

    var line: String? = read.readLine()
    while(line !== null) {
        out.add(line)
        line = read.readLine()
    }

    return out._array
}