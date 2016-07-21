package jardich.easydiff

import java.io.*
import java.util.regex.Pattern

fun main(args: Array<String>) {
    val dir = "./tmp/${System.currentTimeMillis()}/"

//    val repo = "https://github.com/adrijardi/create-your-pizza.git"
//    val fromCommit = "eea4f1046a33149844f7a200e6c3f6beccd5f01e"
//    val toCommit = "b3b20e5357dc17d1e6ec33882814c2ba2530a49a"

    val repo = "git@git.adstream.com:adbank-5/delivery-server.git"
    val fromCommit = "6519b2edad749cec9eae6f239dc20c42130f0686"
    val toCommit = "69a4b61f8ccff572d42ff71a7c46c21a6db5fd0c"
    val regex = "(NIR-\\d+|NGN-\\d+)"


    clone(repo, dir)

    val commits = commitLog(fromCommit, toCommit, dir)

    commits.forEach { println(it) }

    val regRes = applyRegex(commits, Pattern.compile(regex))

    println()
    regRes.forEach { println(it) }

    cleanup(dir)
}

fun clone(repo: String, tempDir: String) {
    val proc = Runtime.getRuntime().exec("git clone $repo $tempDir")

    pipeStreams(proc)
}

fun commitLog(fromCommit: String, toCommit: String, dir: String): List<String> {
    val proc = Runtime.getRuntime().exec("git log --oneline $fromCommit..$toCommit", emptyArray<String>(), File(dir))

    pipeStream(proc.errorStream, System.err)

    return readStream(proc.inputStream)
}

fun applyRegex(input: List<String>, regex: Pattern): List<String> {
    return input.map { regex.matcher(it) }
            .filter { it.find() }
            .map { it.group(0) }
            .filter { it != null }
}

fun cleanup(dir: String) {
    File(dir).deleteOnExit()
}

fun pipeStreams(proc: Process) {
    pipeStream(proc.inputStream, System.out)
    pipeStream(proc.errorStream, System.err)
}

fun pipeStream(from: InputStream, to: OutputStream) {
    val buffer = ByteArray(1000)

    var len = from.read(buffer)
    while(len != -1) {
        to.write(buffer, 0, len)
        len = from.read(buffer)
    }
}

fun readStream(s: InputStream): List<String> {
    val read = BufferedReader(InputStreamReader(s))
    val out = mutableListOf<String>()

    var line: String? = read.readLine()
    while(line !== null) {
        out.add(line)
        line = read.readLine()
    }

    return out
}