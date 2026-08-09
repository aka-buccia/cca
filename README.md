# CCA - Choreography Correctness Analyzer

Choreography Correctness Analyzer is a static correctness analyzer for FaaSChalCore choreographies.

## What's FaaSChalCore?

FaaSChalCore is a choreographic calculus containing the key features of the FaaSChal language, and for which a static analysis discipline is defined.
FaaSChal is a coreographic programming language tailored for serverless Function-as-a-Service architectures.

## Features

- Parser
- PrettyPrinter
- Static Checker (in development)

## Building the Project

**Run `mvn install`** from the project directory. This command generates **faasch.jar** (a self-contained JAR with all dependencies included) in the `target` directory.

Execute the JAR directly with:

```bash
java -jar faasch.jar [COMMAND] [OPTIONS]
```

### Convenient Setup

To use `faasch` from anywhere without specifying the full path, set up environment variables:

1. **Set the PATH variable** to include the scripts directory:

   ```bash
   export PATH="PATH_TO_CCA_DIR/scripts:$PATH"
   ```

2. **Set the FAASCHALCORE_HOME variable** to the JAR location:

   ```bash
   export FAASCHALCORE_HOME="PATH_TO_CCA_DIR/target"
   ```

   Replace `PATH_TO_CCA_DIR` with your actual project directory.

You can add both lines to your `.bashrc` file, this way you won't need to run them each time you open a terminal.

The **faasch.jar is completely self-contained** and can be moved anywhere. You just need tu update `FAASCHALCORE_HOME` accordingly to the JAR location. Also, if you place the **faasch bash script** in a standard bin directory there's no need to set or modify the `PATH`.

## Credits

- Based on Mauro Vergnani's thesis _Formalisation and Static Analysis for the Serverless Choreographic Programming Language FaaSChal_
- Project structure strongly inspired from [Choral](https://github.com/choral-lang/choral)
