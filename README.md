## Introduction
This repository contains samples for EXD vData SDK for Java package (exceeddata-sdk-vdata).  vData is an edge database running on vehicles' domain controllers.  It stores signal data in a high-compression file format with the extension of .vsw.  EXD vData SDK offers vsw decoding capabilities in standard programming languages such as C++, [Java](https://github.com/exceeddata/sdk-vdata-java), [Python](https://github.com/exceeddata/sdk-vdata-python), [Javascript](https://github.com/exceeddata/sdk-vdata-javascript), and etc.  

The following sections demonstrates how to install and use the SDK.

## Table of Contents
- [System Requirement](#system-requirement)
- [Additional Dependencies](#additional-dependencies)
- [License](#license)
- [Installation](#installation)
- [API Documentation](#api-documentation)
- [Sample Usage](#sample-usage)
- [Complete Examples](#complete-examples)
  - [Full VSW Decode App](#full-vsw-decode-app)
  - [Convert VSW to ASC Format](#convert-vsw-to-asc-format)
  - [Convert VSW to BLF Format](#convert-vsw-to-blf-format)
- [Getting Help](#getting-help)
- [Contributing to EXD](#contributing-to-exd)

## System Requirement
- Java 8+

## Additional Dependencies
The following dependencies are needed.
- aircompressor
  ```xml
    <dependency>
	    <groupId>io.airlift</groupId>
	    <artifactId>aircompressor</artifactId>
	    <version>0.24</version>
	  </dependency>
  ```

## License
The codes in the repository are released with [MIT License](LICENSE).

## Installation
Binary installers for the latest released version are available upon request.


## API Documentation
Publicly available SDK classes and methods are at [API Documentation](https://htmlpreview.github.io/?https://github.com/exceeddata/sdk-vdata-java/blob/main/doc/index.html).

**Import**
To use the SDK import the following:
- com.exceeddata.sdk.vdata.data.* which includes the following classes:
  - VDataReaderFactory: factory class which creates a VDataReader object
  - VDataReader: reads vsw files
  - VDataMeta: the metadata information of the vsw file
  - VDataFrame: the data frame of the vsw data contents
  - VDataRow: if using iterator() method to iterate row-by-row

## Sample Usage
SDK is very easy to use, in most cases 10 lines of code is sufficient.

```java
byte[] bytes = Files.readAllBytes(Paths.get(....);
String signals = ""; // replace with comma-delimited list of signal names if needed
VDataReader reader = new VDataReaderFactory()
                            .setData(bytes)
                            .setSignals(signals)
                            .open();
VDataFrame df = reader.df();
List<String> cols = df.cols(true);
Object[][] objs = df.objects();
```

## Complete Examples
### Full VSW Decode App
- [VswDecode.java](example/src/main/java/com/exceeddata/examples/VswDecode.java): a full parameterized app that decodes vsw then exports to CSV format.
  - Supports input and output file path parameters.
  - Supports optional signals selection parameter.
  - Supports optional base64 encoded vsw files.
  - Supports densify, expand mode and queue mode parameters.


## Getting Help
For usage questions, the best place to go to is [Github issues](https://github.com/exceeddata/sdk-vdata-java/issues). For customers of EXCEEDDATA commercial solutions, you can contact [support](mailto:support@smartsct.com) for questions or support.

## Contributing to EXD
All contributions, bug reports, bug fixes, documentation improvements, code enhancements, and new ideas are welcome.

<hr>

[Go to Top](#table-of-contents)
