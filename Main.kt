package wordsvirtuoso
import java.io.*
import kotlin.system.exitProcess
import kotlin.random.Random

fun hasDuplicateCharacters(string: String): Boolean {
    val map = string.groupingBy { it }.eachCount()
    var answer = false
    for ((key,value ) in map) {
        if (value != 1) answer = true
    }
    return answer
}

fun checkWord(word: String): Boolean {
    var isValid = true
    when {
        word.length != 5 -> isValid = false
        !word.matches("[a-zA-Z]{5}".toRegex()) -> isValid = false
        hasDuplicateCharacters(word) -> isValid = false
    }
    return isValid
}

fun countInvalidWords(fileName: String): Int {
    var numberOfInvalidWords = 0
    val words = File(fileName).readLines()
    for (word in words) {
        word.trim().lowercase()
        if (!checkWord(word)) {
            numberOfInvalidWords ++
        }
    }
    return numberOfInvalidWords
}

fun countNotIncluded(allWords: String, candidatesFile: String): Int {
    var numberOfNotIncluded = 0
    val candidates = File(candidatesFile).readLines()
    for (word in candidates) {
        if (word.lowercase() !in File(allWords).readLines().map { it.lowercase() }) {
            numberOfNotIncluded ++
        }
    }
    return numberOfNotIncluded
}

fun exit() {
    println("The game is over.")
    exitProcess(1)
}

fun createClue(secretWord: String, guessWord: String,wrong: MutableList<String>, list: MutableList<String>) {
    var clue = ""
    guessWord.lowercase()
    secretWord.lowercase()
    for (i in 0..4) {
        if (guessWord[i] !in secretWord) {
            clue += "\u001B[48:5:7m${guessWord[i].uppercase()}\u001B[0m"
            wrong.add(guessWord[i].uppercase())
        } else {
            clue += if (guessWord[i] == secretWord[i]) {
                "\u001B[48:5:10m${guessWord[i].uppercase()}\u001B[0m"
            } else {
                "\u001B[48:5:11m${guessWord[i].uppercase()}\u001B[0m"
            }
        }
    }
    list.add(clue)
}

fun playerInputIsValid(inputString: String, allWords: String): Boolean {
    inputString.lowercase()
    if (inputString == "exit") exit()
    var answer = true
    when {
        inputString.length != 5 -> {
            println("The input isn't a 5-letter word.")
            answer =  false
        }
        !inputString.matches("[a-zA-Z]{5}".toRegex()) ->  {
            println("One or more letters of the input aren't valid.")
            answer =  false
        }
        hasDuplicateCharacters(inputString) ->  {
            println("The input has duplicate letters.")
            answer = false
        }
        inputString !in File(allWords).readLines().map { it.lowercase() } ->  {
            println("The input word isn't included in my words list.")
            answer = false
        }
    }
    return answer
}

fun correct(start: Long, turns: Int) {
    println("Correct!")
    val end = System.currentTimeMillis()
    val duration = (end - start) / 1000
    println("The solution was found after $turns tries in ${duration.toInt()} seconds.")
}

fun main(vararg fileNames: String) {

    var numberOfArguments = 0
    for (argument in fileNames) {
        numberOfArguments += 1
    }

    if (numberOfArguments != 2) {
        println("Error: Wrong number of arguments.")
        exitProcess(1)
    }

    val allWordsFileName = fileNames.first()
    val candidatesFileName = fileNames.last()

    if (!File(allWordsFileName).exists()) {
        println("Error: The words file $allWordsFileName doesn't exist.")
        exitProcess(1)
    }

    if (!File(candidatesFileName).exists()) {
        println("Error: The candidate words file $candidatesFileName doesn't exist.")
        exitProcess(1)
    }

    val invalidWordsInAllWords = countInvalidWords(allWordsFileName)
    val invalidCandidatesWords = countInvalidWords(candidatesFileName)

    if (invalidWordsInAllWords != 0) {
        println("Error: $invalidWordsInAllWords invalid words were found in the $allWordsFileName file.")
        exitProcess(1)
    }

    if (invalidCandidatesWords != 0) {
        println("Error: $invalidCandidatesWords invalid words were found in the $candidatesFileName file.")
        exitProcess(1)
    }

    val numberOfNotIncludedCandidates = countNotIncluded(allWordsFileName, candidatesFileName)
    if (numberOfNotIncludedCandidates != 0) {
        println("Error: $numberOfNotIncludedCandidates candidate words are not included in the $allWordsFileName file.")
        exitProcess(1)
    }

    println("Words Virtuoso")
    val numberOfCandidates = File(candidatesFileName).readLines().size
    val secretWord = File(candidatesFileName).readLines()[Random.nextInt(0, numberOfCandidates)].lowercase()

    println("Input a 5-letter word:")
    val start = System.currentTimeMillis()
    var numberOfTurns = 1
    val listOfClues = mutableListOf<String>()
    val wrongChars = mutableListOf<String>()

    val firstAttempt = readln()
    if (firstAttempt.lowercase() == secretWord.lowercase()) {
        for (letter in secretWord) {
            print("\u001B[48:5:10m${letter.uppercase()}\u001B[0m")
        }
        println("Correct!")
        println("Amazing luck! The solution was found at once.")
    } else {
        if (playerInputIsValid(firstAttempt, allWordsFileName)) {
            createClue(secretWord, firstAttempt, wrongChars, listOfClues)
            for (element in listOfClues) {
                println(element)
            }
            println("\u001B[48:5:14m${wrongChars.sorted().joinToString("")}\u001B[0m")
        }
        numberOfTurns ++
        while (true) {
            println("Input a 5-letter word:")
            val newAttempt = readln()
            if (newAttempt.lowercase() == secretWord.lowercase()) {
                createClue(secretWord, newAttempt, wrongChars, listOfClues)
                for (element in listOfClues) {
                    println(element)
                }
                correct(start, numberOfTurns)
                exitProcess(1)
            } else {
                if (playerInputIsValid(newAttempt, allWordsFileName)) {
                    createClue(secretWord, newAttempt, wrongChars, listOfClues)
                    for (element in listOfClues) {
                        println(element)
                    }
                    println("\u001B[48:5:14m${wrongChars.sorted().toSet().joinToString("")}\u001B[0m")
                }
                numberOfTurns ++
            }
        }
    }
}
