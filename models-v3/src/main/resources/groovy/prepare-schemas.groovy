import groovy.json.JsonOutput

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static groovy.io.FileType.FILES

TYPE = "{type}"
SCHEMA_DIR_PATH = "models-v3/src/main/resources/schemas/avro/{type}/"
OUTPUT_DIR_PATH = "models-v3/target/schemas/avro/"

class Schema {
    String type = "AVRO"
    String schema
    Map properties = ["__alwaysAllowNull": "true", "__jsr310ConversionEnabled": "false"]
}

def prepareSchemaFiles(String type) {
    def schemaDir = new File(SCHEMA_DIR_PATH.replace(TYPE, type))
    def outputDir = new File(OUTPUT_DIR_PATH + File.separatorChar + type)
    outputDir.mkdirs()

    schemaDir.traverse(type: FILES, nameFilter: ~/.*\.avsc/) { schemaFile ->
        def schema = new Schema()
        schema.schema = schemaFile.text
            .replaceAll("\\s", "")
//            .replaceAll("\"namespace\":\"((\\w)*\\.)+(\\w)*\",?", "")
        def schemaFileName = schemaFile.name
        schemaFileName = schemaFileName.take(schemaFileName.lastIndexOf(".")) + ".json"
        def subPath = schemaFile.parentFile.path.replace(schemaDir.path, "")
        def path = outputDir.path + File.separatorChar + subPath;
        new File(path).mkdirs()
        def outputFile = new File(path + File.separator + schemaFileName)
        outputFile.text = JsonOutput.toJson(schema);
    }
}

def zip() {
    def schemasDir = new File(OUTPUT_DIR_PATH)
    def zipFile = new File(OUTPUT_DIR_PATH + "schemas.zip")

    ZipOutputStream zipInputStream = new ZipOutputStream(new FileOutputStream(zipFile))
    schemasDir.eachFileRecurse({ file ->
        if (file == zipFile) {
            return
        }
        zipInputStream.putNextEntry(new ZipEntry(file.path - schemasDir.path + (file.directory ? "/" : "")))
        if (file.file) {
            zipInputStream << file.bytes
        }
        zipInputStream.closeEntry()
    })
    zipInputStream.close()
}

def prepareSchemas() {
    prepareSchemaFiles("test")

    zip()
}

prepareSchemas()