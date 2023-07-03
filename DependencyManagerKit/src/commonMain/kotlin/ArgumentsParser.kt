package dependency.manager.kit

class ArgumentsParser {
    fun parse(args: Array<String>): Dependency? {
        if (args.isEmpty()) {
            println("No arguments provided. Will use defaults. Usage: --dependency <dependency>.")
            return null
        }

        if (args.size > 2) {
            println("Too many arguments. To provide root dependency use: --dependency <dependency>")
            return null
        }

        when(args.first()) {
            "--dependency" -> {
                if (args.size < 2) {
                    println("Please provide a dependency to resolve.")
                    return null
                }
                return Dependency(args[1])
            }
            "--help" -> {
                println("Usage: --dependency <dependency>")
                return null
            }
            else -> { return null }
        }
    }
}