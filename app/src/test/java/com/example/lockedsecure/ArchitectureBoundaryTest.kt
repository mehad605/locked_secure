package com.example.lockedsecure

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Automated architecture guard enforcing S3 layering, feature isolation, cycle freedom,
 * and size/hygiene constraints across all production Kotlin sources.
 */
class ArchitectureBoundaryTest {
    private val mainSrcDir: File =
        listOf(
            File("src/main/java"),
            File("app/src/main/java"),
        ).first { it.isDirectory }

    private fun productionKotlinFiles(): List<File> =
        mainSrcDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .sorted()
            .toList()

    private fun parsePackage(file: File): String =
        file
            .readLines()
            .firstOrNull { it.startsWith("package ") }
            ?.removePrefix("package ")
            ?.trim()
            .orEmpty()

    private fun parseImports(file: File): List<String> =
        file
            .readLines()
            .filter { it.startsWith("import ") }
            .map { it.removePrefix("import ").trim() }

    @Test
    fun domainLayerHasZeroAndroidDataOrUiDependencies() {
        val violations = mutableListOf<String>()
        for (file in productionKotlinFiles()) {
            val pkg = parsePackage(file)
            if (!pkg.contains(".domain")) continue
            for (imp in parseImports(file)) {
                if (imp.startsWith("android.") || imp.contains(".data.") || imp.contains(".ui.")) {
                    violations += "${file.name} ($pkg) illegally imports $imp"
                }
            }
        }
        assertTrue("Domain layer violations:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun uiLayerNeverImportsDataLayerDirectly() {
        val violations = mutableListOf<String>()
        for (file in productionKotlinFiles()) {
            val pkg = parsePackage(file)
            if (!pkg.contains(".ui")) continue
            for (imp in parseImports(file)) {
                if (imp.contains(".data.")) {
                    violations += "${file.name} ($pkg) illegally imports data type $imp"
                }
            }
        }
        assertTrue("UI -> data violations:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun featuresDoNotImportOtherFeatureInternals() {
        val featureRegex = Regex("""com\.example\.lockedsecure\.feature\.([a-z0-9_]+)""")
        val violations = mutableListOf<String>()
        for (file in productionKotlinFiles()) {
            val pkg = parsePackage(file)
            val ownFeature = featureRegex.find(pkg)?.groupValues?.get(1) ?: continue
            for (imp in parseImports(file)) {
                val targetFeature = featureRegex.find(imp)?.groupValues?.get(1) ?: continue
                if (targetFeature != ownFeature && !imp.contains(".domain.")) {
                    violations += "${file.name} ($ownFeature) illegally imports non-domain $imp"
                }
            }
        }
        assertTrue("Cross-feature violations:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun packageDependencyGraphHasZeroCycles() {
        val allPackages = productionKotlinFiles().map { parsePackage(it) }.toSet()
        val edges = buildPackageEdges(allPackages)
        val cycles = detectCycles(allPackages, edges)
        assertTrue("Circular package dependencies:\n${cycles.joinToString("\n")}", cycles.isEmpty())
    }

    private fun buildPackageEdges(allPackages: Set<String>): Map<String, Set<String>> {
        val edges = mutableMapOf<String, MutableSet<String>>()
        for (file in productionKotlinFiles()) {
            val fromPkg = parsePackage(file)
            val targets = edges.getOrPut(fromPkg) { mutableSetOf() }
            for (imp in parseImports(file)) {
                // Skip build-generated Android R resource index class.
                if (imp == "com.example.lockedsecure.R") continue
                val targetPkg =
                    allPackages
                        .filter { it != fromPkg && imp.startsWith("$it.") }
                        .maxByOrNull { it.length }
                if (targetPkg != null) {
                    targets += targetPkg
                }
            }
        }
        return edges
    }

    private fun detectCycles(
        allPackages: Set<String>,
        edges: Map<String, Set<String>>,
    ): List<String> {
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        val cycles = mutableListOf<String>()

        fun dfs(
            node: String,
            path: List<String>,
        ) {
            visiting += node
            for (next in edges[node].orEmpty()) {
                if (next in visiting) {
                    cycles += (path + next).joinToString(" -> ")
                } else if (next !in visited) {
                    dfs(next, path + next)
                }
            }
            visiting -= node
            visited += node
        }

        for (pkg in allPackages) {
            if (pkg !in visited) dfs(pkg, listOf(pkg))
        }
        return cycles
    }

    @Test
    fun productionFilesRespectSizeAndHygieneLimits() {
        val violations = mutableListOf<String>()
        val fnRegex =
            Regex(
                """^\s*(?:(?:private|public|internal|protected|override|inline|suspend|open)\s+)*fun\s+(\w+)""",
            )
        for (file in productionKotlinFiles()) {
            val lines = file.readLines()
            if (lines.size > 300) {
                violations += "${file.name}: ${lines.size} lines exceeds 300-line limit"
            }
            lines.forEachIndexed { idx, line ->
                if (line.contains("!!")) {
                    violations += "${file.name}:${idx + 1}: forbidden '!!' operator"
                }
                if (line.contains("lateinit ")) {
                    violations += "${file.name}:${idx + 1}: forbidden 'lateinit'"
                }
                if (line.trimStart().startsWith("import ") && line.trimEnd().endsWith(".*")) {
                    violations += "${file.name}:${idx + 1}: forbidden wildcard import"
                }
                val match = fnRegex.find(line)
                if (match != null) {
                    val fnLen = measureBraceBlockLength(lines, idx)
                    if (fnLen > 40) {
                        violations += "${file.name}:${idx + 1} fun ${match.groupValues[1]} is $fnLen lines (>40)"
                    }
                }
            }
        }
        assertTrue("Code hygiene violations:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    private fun measureBraceBlockLength(
        lines: List<String>,
        startIndex: Int,
    ): Int {
        var depth = 0
        var started = false
        for (i in startIndex until lines.size) {
            for (ch in lines[i]) {
                if (ch == '{') {
                    depth++
                    started = true
                } else if (ch == '}') {
                    depth--
                }
            }
            if (started && depth == 0) {
                return i - startIndex + 1
            }
        }
        return 1
    }
}
