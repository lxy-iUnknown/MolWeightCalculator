buildscript {
    val elementData = mapOf(
        Pair("H", 1.007975),
        Pair("He", 4.002602),
        Pair("Li", 6.9675),
        Pair("Be", 9.0121831),
        Pair("B", 10.8135),
        Pair("C", 12.0106),
        Pair("N", 14.006855),
        Pair("O", 15.9994),
        Pair("F", 18.998403162),
        Pair("Ne", 20.1797),
        Pair("Na", 22.98976928),
        Pair("Mg", 24.3055),
        Pair("Al", 26.9815384),
        Pair("Si", 28.085),
        Pair("P", 30.973761998),
        Pair("S", 32.0675),
        Pair("Cl", 35.4515),
        Pair("Ar", 39.95),
        Pair("K", 39.0983),
        Pair("Ca", 40.078),
        Pair("Sc", 44.955907),
        Pair("Ti", 47.867),
        Pair("V", 50.9415),
        Pair("Cr", 51.9961),
        Pair("Mn", 54.938043),
        Pair("Fe", 55.845),
        Pair("Co", 58.933194),
        Pair("Ni", 58.6934),
        Pair("Cu", 63.546),
        Pair("Zn", 65.38),
        Pair("Ga", 69.723),
        Pair("Ge", 72.63),
        Pair("As", 74.921595),
        Pair("Se", 78.971),
        Pair("Br", 79.904),
        Pair("Kr", 83.798),
        Pair("Rb", 85.4678),
        Pair("Sr", 87.62),
        Pair("Y", 88.905838),
        Pair("Zr", 91.224),
        Pair("Nb", 92.90637),
        Pair("Mo", 95.95),
        Pair("Tc", 98.0),
        Pair("Ru", 101.07),
        Pair("Rh", 102.90549),
        Pair("Pd", 106.42),
        Pair("Ag", 107.8682),
        Pair("Cd", 112.414),
        Pair("In", 114.818),
        Pair("Sn", 118.71),
        Pair("Sb", 121.76),
        Pair("Te", 127.6),
        Pair("I", 126.90447),
        Pair("Xe", 131.293),
        Pair("Cs", 132.90545196),
        Pair("Ba", 137.327),
        Pair("La", 138.90547),
        Pair("Ce", 140.116),
        Pair("Pr", 140.90766),
        Pair("Nd", 144.242),
        Pair("Pm", 145.0),
        Pair("Sm", 150.36),
        Pair("Eu", 151.964),
        Pair("Gd", 157.25),
        Pair("Tb", 158.925354),
        Pair("Dy", 162.5),
        Pair("Ho", 164.930329),
        Pair("Er", 167.259),
        Pair("Tm", 168.934219),
        Pair("Yb", 173.045),
        Pair("Lu", 174.9668),
        Pair("Hf", 178.486),
        Pair("Ta", 180.94788),
        Pair("W", 183.84),
        Pair("Re", 186.207),
        Pair("Os", 190.23),
        Pair("Ir", 192.217),
        Pair("Pt", 195.084),
        Pair("Au", 196.96657),
        Pair("Hg", 200.592),
        Pair("Tl", 204.3835),
        Pair("Pb", 207.2),
        Pair("Bi", 208.9804),
        Pair("Po", 209.0),
        Pair("At", 210.0),
        Pair("Rn", 222.0),
        Pair("Fr", 223.0),
        Pair("Ra", 226.0),
        Pair("Ac", 227.0),
        Pair("Th", 232.0377),
        Pair("Pa", 231.03588),
        Pair("U", 238.02891),
        Pair("Np", 237.0),
        Pair("Pu", 244.0),
        Pair("Am", 243.0),
        Pair("Cm", 247.0),
        Pair("Bk", 247.0),
        Pair("Cf", 251.0),
        Pair("Es", 252.0),
        Pair("Fm", 257.0),
        Pair("Md", 258.0),
        Pair("No", 259.0),
        Pair("Lr", 266.0),
        Pair("Rf", 267.0),
        Pair("Db", 268.0),
        Pair("Sg", 269.0),
        Pair("Bh", 270.0),
        Pair("Hs", 277.0),
        Pair("Mt", 278.0),
        Pair("Ds", 281.0),
        Pair("Rg", 282.0),
        Pair("Cn", 285.0),
        Pair("Nh", 286.0),
        Pair("Fl", 289.0),
        Pair("Mc", 290.0),
        Pair("Lv", 293.0),
        Pair("Ts", 294.0),
        Pair("Og", 294.0),
    )

    // Keep in sync with ElementId.kt
    val elementIdBase = 26
    val elementIdMax = elementIdBase + elementIdBase * elementIdBase - 1

    fun parseElementId(firstChar: Char): Int {
        // Keep in sync with ElementId.kt
        return firstChar.code - 'A'.code
    }

    fun parseElementId(firstChar: Char, secondChar: Char): Int {
        // Keep in sync with ElementId.kt
        return firstChar.code * 26 + secondChar.code - (('A'.code - 1) * 26 + 'a'.code)
    }

    fun parseElementData(): Pair<String, String> {
        val arraySize = elementIdMax + 1
        val elementOrdinals = IntArray(arraySize)
        val elementWeights = DoubleArray(arraySize)
        var ordinal = 1

        elementOrdinals.fill(-1, 0, arraySize)
        elementWeights.fill(Double.NaN, 0, arraySize)

        elementData.forEach { (name, weight) ->
            val elementId = when (val nameLength = name.length) {
                1 -> parseElementId(name[0])
                2 -> parseElementId(name[0], name[1])
                else -> throw RuntimeException(
                    "Invalid element name length $nameLength")
            }
            if (weight <= 0) {
                throw RuntimeException("Non-positive element weight $weight")
            }
            if (!weight.isFinite()) {
                throw RuntimeException("Non-finite element weight $weight")
            }
            elementWeights[elementId] = weight
            elementOrdinals[elementId] = ordinal++
        }
        return Pair(
            elementOrdinals.joinToString(prefix = "{", postfix = "}"),
            elementWeights.joinToString(prefix = "{", postfix = "}") {
                if (it.isNaN()) "Double.NaN" else it.toString()
            }
        )
    }

    extra["parseElementData"] = ::parseElementData
}