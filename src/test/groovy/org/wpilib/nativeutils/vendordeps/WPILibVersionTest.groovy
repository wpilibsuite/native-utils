package org.wpilib.nativeutils.vendordeps

import spock.lang.Specification

class WPILibVersionTest extends Specification {
    def "three and four part versions compare by their first three parts"() {
        expect:
        WPILibVersion.parse(threePart) == WPILibVersion.parse(fourPart)

        where:
        threePart   | fourPart
        '2027.1-1'  | '2027.1-1.0'
        '2027.1-1'  | '2027.1-1.9'
        '2027.10-1' | '2027.10-1.0'
        '2027.01-1' | '2027.1-1.0'
    }

    def "differences in the first three parts are detected"() {
        expect:
        WPILibVersion.parse(a) != WPILibVersion.parse(b)

        where:
        a          | b
        '2027.1-1' | '2028.1-1'
        '2027.1-1' | '2027.2-1'
        '2027.1-1' | '2027.1-2'
    }

    def "malformed versions are rejected"() {
        when:
        WPILibVersion.parse(value)

        then:
        thrown(InvalidWPILibVersionException)

        where:
        value << ['', '2027', '2027.1', '2027.1-', '2027.1-1.', '2027.a-1.0',
                  ' 2027.1-1.0', '2027.1-1.0 ', '2027.1-1.0.2', '-1.1-1.0', null]
    }

    def "old YEAR.MAJOR.MINOR scheme is rejected"() {
        when:
        WPILibVersion.parse(value)

        then:
        thrown(InvalidWPILibVersionException)

        where:
        value << [
                '2027.1.0',
                '2027.1.0-alpha-1',
                '2027.1.0-beta-2',
                '2027.1.0-rc-3',
        ]
    }
}
