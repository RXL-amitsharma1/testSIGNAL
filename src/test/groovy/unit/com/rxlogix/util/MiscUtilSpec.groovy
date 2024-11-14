package com.rxlogix.util

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class MiscUtilSpec extends Specification {
    @Ignore
    void "md5ChecksumForFile should return the checksum of a file as a String" () {
        // This needs to be operating system independent. This test can fail on a Windows environment if it has line breaks:
        // http://stackoverflow.com/questions/5940514/is-a-md5-hash-of-a-file-unique-on-every-system
        expect:
            "46190f059ea1c0af37a772c920c1eb53" == MiscUtil.md5ChecksumForFile("test/unit/data/md5checksum_test.txt")
    }
}
