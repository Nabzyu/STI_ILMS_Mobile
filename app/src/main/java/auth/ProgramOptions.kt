package com.example.stismartlib.ui.auth

enum class ProgramOption(val displayName: String, val isSeniorHigh: Boolean) {
    STEM("STEM (Senior High School)", true),
    ABM("ABM (Senior High School)", true),
    TVL("TVL (Senior High School)", true),
    BSIT("BSIT (College)", false),
    BSTM("BSTM (College)", false),
    BSHM("BSHM (College)", false);

    companion object {
        val allDisplayNames: List<String> = entries.map { it.displayName }

        fun fromDisplayName(name: String): ProgramOption? =
            entries.find { it.displayName == name }
    }
}

fun yearLevelsForProgram(programName: String): List<String> {
    val program = ProgramOption.fromDisplayName(programName) ?: return emptyList()
    return if (program.isSeniorHigh) {
        listOf("Grade 11", "Grade 12")
    } else {
        listOf("1st Year", "2nd Year", "3rd Year", "4th Year")
    }
}
