package global.genesis.notify.utils

import org.apache.commons.compress.compressors.CompressorOutputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.Base64

class MessageCompressor {
    fun getBodyDecompress(compressedBody: String, compressionType: String? = null): String {
        if (compressedBody.isEmpty() || compressionType == null) {
            return compressedBody
        }

        val outStr = StringBuilder()
        val gis = CompressorStreamFactory.getSingleton()
            .createCompressorInputStream(
                compressionType,
                ByteArrayInputStream(Base64.getDecoder().decode(compressedBody))
            )

        val bufferedReader = BufferedReader(InputStreamReader(gis, "UTF-8"))
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            outStr.append(line)
        }
        return outStr.toString()
    }

    fun compress(str: String, compressionType: String? = null): String {
        if (str.isEmpty() || compressionType == null) {
            return str
        }
        val obj = ByteArrayOutputStream()
        val gzip: CompressorOutputStream = CompressorStreamFactory.getSingleton()
            .createCompressorOutputStream(compressionType, obj)
        gzip.write(str.toByteArray(charset("UTF-8")))
        gzip.flush()
        gzip.close()
        return Base64.getEncoder().encodeToString(obj.toByteArray())
    }
}
