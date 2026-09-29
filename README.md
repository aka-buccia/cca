# CCA - Choreography Correctness Analyzer

Choreography Correctness Analyzer is a static correctness analyzer for FaaSChalCore choreographies.

## What's FaaSChalCore?

FaaSChalCore is a choreographic calculus containing the key features of the FaaSChal language, and for which a static analysis discipline is defined.
FaaSChal is a coreographic programming language tailored for serverless Function-as-a-Service architectures.

## Features

- Parser
- PrettyPrinter
- Static Checker

## Why?

The static analysis discipline for FaaSChalCore was formally defined and proven correct in prior work (see References). This thesis implements that discipline as a practical, usable tool.

In serverless architectures, functions are stateless and short-lived. Choreographies can produce *dangling functions*:

- **Leak functions**: stateless instances that remain active after execution completes
- **Orphan functions**: stateless instances unable to send their response back

The Static Checker detects these errors *before* execution by statically analyzing the program without running it. A choreography that passes the check is **well-formed**—formally guaranteed not to produce dangling functions.

The checker makes this formal guarantee accessible through contextualized error reporting and additional checks.

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

The **faasch.jar is completely self-contained** and can be moved anywhere. You just need to update `FAASCHALCORE_HOME` accordingly to the JAR location. Also, if you place the **faasch bash script** in a standard bin directory there's no need to set or modify the `PATH`.

## How to use

**Pretty-print a program:**

```bash
faasch prettify -s=sourcefile.faasch
```

Formats the source code in a readable way overwriting the file.

**Check a program:**

```bash
faasch check -s=sourcefile.faasch
```

Performs static analysis and reports any well-formedness violations.

Use flag `-h` for more options.

## References & Credits

- **Thesis**: Mauro Vergnani, [*Formalisation and Static Analysis for the Serverless Choreographic Programming Language FaaSChal*](https://amslaurea.unibo.it/id/eprint/39077/), University of Bologna.

- **Paper**: Saverio Giallorenzo et al., A Choreographic Calculus for Dangling-Function-Free Microservice-Serverless Systems, [https://doi.org/10.5281/zenodo.23006287](https://doi.org/10.5281/zenodo.23006287).

- Project structure strongly inspired from [Choral](https://github.com/choral-lang/choral)
