import revxrsal.commands.autocomplete.AutoCompleter
import revxrsal.commands.minestom.MinestomLamp
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class CommandRegistrar {

    fun reflect() {
        // Starts the lamp command builder
        val lamp = MinestomLamp.builder().build()

        val packageName = "net.hellz.commands"
        val classLoader = Thread.currentThread().contextClassLoader
        val path = packageName.replace('.', '/')
        val resources = classLoader.getResources(path)
        val dirs = mutableListOf<File>()

        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            dirs.add(File(resource.file))
        }

        val classes = mutableListOf<Class<*>>()
        for (directory in dirs) {
            classes.addAll(findClasses(directory, packageName))
        }

        // Registers the commands
        classes.filter { it != CommandRegistrar::class.java }.forEach { commandClass ->
            try {
                // Ensure we're registering the actual command class, not synthetic ones
                if (!commandClass.name.contains("$")) {
                    val instance = createCommandInstance(commandClass)
                    lamp.register(instance)
                    println("Registered command: ${commandClass.simpleName}")
                }
            } catch (e: Exception) {
                println("Failed to register command: ${commandClass.simpleName}")
                e.printStackTrace()
            }
        }
    }

    // Creates the instance for the commands using Java Reflection
    private fun createCommandInstance(commandClass: Class<*>): Any {
        return try {
            // Ensure we're not calling a synthetic class or lambda
            val constructor: Constructor<*> = commandClass.getConstructor()
            constructor.newInstance()
        } catch (e: NoSuchMethodException) {
            // If no default constructor exists, try calling the default constructor without parameters
            commandClass.getDeclaredConstructor().newInstance()
        }
    }

    // Looks for the classes in the package
    private fun findClasses(directory: File, packageName: String): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        if (!directory.exists()) {
            return classes
        }
        val files = directory.listFiles() ?: return classes
        for (file in files) {
            if (file.isDirectory) {
                classes.addAll(findClasses(file, "$packageName.${file.name}"))
            } else if (file.name.endsWith(".class")) {
                val className = "$packageName.${file.name.substring(0, file.name.length - 6)}"
                classes.add(Class.forName(className))
            }
        }
        return classes
    }
}
