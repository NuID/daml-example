<p align="right"><a href="https://nuid.io"><img src="https://nuid.io/svg/logo.svg" width="20%"></a></p>
<p align="right"><a href="https://daml.com"><img src="https://docs.daml.com/_static/images/DAML_Logo_Blue.svg" width="20%"></a></p>

# NuID :: DAML

NuID's distributed key management backed by DAML's smart contract platform.


## Requirements

* [npm](https://nodejs.org/en/download/)
* [clojure](https://clojure.org/guides/getting_started) & [boot](https://github.com/boot-clj/boot#install)
* [DAML SDK](https://docs.daml.com/getting-started/installation.html#)

## Usage

From the root of this directory:

```
$ chmod +x bin/demo.sh
$ ./bin/demo.sh
```

Once `Server started!` appears in the terminal output, you'll be able to visit:
* `localhost:4000` to inspect the DAML navigator
* `localhost:8080` to interact with a minimal login demo
* `localhost:8080/slides` for additional information on using NuID :: DAML (navigate by clicking the top or bottom half of the page)

Exit and cleanup with `ctrl+c` (twice, on some platforms).
