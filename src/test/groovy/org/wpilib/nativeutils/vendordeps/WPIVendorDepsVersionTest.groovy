package org.wpilib.nativeutils.vendordeps

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.TempDir

class WPIVendorDepsVersionTest extends Specification {
    @TempDir
    File tempDir

    private WPIVendorDepsExtension extension() {
        def project = ProjectBuilder.builder().build()
        return project.extensions.create('wpiVendorDeps', WPIVendorDepsExtension, project)
    }

    private void writeVendordep(String fieldLine) {
        String field = fieldLine ? fieldLine + ',' : ''
        new File(tempDir, 'test.json').text = """{
  "name": "TestDep",
  "uuid": "test-uuid",
  $field
  "mavenUrls": ["https://example.com/maven"],
  "cppDependencies": [],
  "jniDependencies": [],
  "javaDependencies": []
}"""
    }

    def "matching versions pass and nonbreaking differences are ignored"() {
        given:
        def ext = extension()
        ext.wpilibVersion.set('2027.1-1.0')
        writeVendordep('"wpilibVersion": "2027.1-1.9"')

        when:
        ext.loadFrom(tempDir)
        ext.validateDependencies()

        then:
        noExceptionThrown()
    }

    def "breaking version differences fail"() {
        given:
        def ext = extension()
        ext.wpilibVersion.set('2027.1-1.0')
        writeVendordep('"wpilibVersion": "2027.1-2.0"')

        when:
        ext.loadFrom(tempDir)
        ext.validateDependencies()

        then:
        thrown(InvalidVendorDepVersionException)
    }

    def "a vendordep without wpilibVersion fails when a version is configured"() {
        given:
        def ext = extension()
        ext.wpilibVersion.set('2027.1-1.0')
        writeVendordep('')

        when:
        ext.loadFrom(tempDir)
        ext.validateDependencies()

        then:
        thrown(InvalidVendorDepVersionException)
    }

    def "an unset configured version skips the version comparison"() {
        given:
        def ext = extension()
        writeVendordep('"wpilibVersion": "1999.9-9.0"')

        when:
        ext.loadFrom(tempDir)
        ext.validateDependencies()

        then:
        noExceptionThrown()
    }

    def "legacy wpilibYear fails during validation even with no configured version"() {
        given:
        def ext = extension()
        writeVendordep('"wpilibYear": "2027"')

        when:
        ext.loadFrom(tempDir)
        ext.validateDependencies()

        then:
        thrown(LegacyVendorDepYearException)
    }

    def "a malformed configured version fails"() {
        given:
        def ext = extension()
        ext.wpilibVersion.set('2027.1')
        writeVendordep('"wpilibVersion": "2027.1-1.0"')

        when:
        ext.validateDependencies()

        then:
        thrown(InvalidWPILibVersionException)
    }

    def "old scheme in the configured version fails"() {
        given:
        def ext = extension()
        ext.wpilibVersion.set(oldVersion)
        writeVendordep('"wpilibVersion": "2027.1-1.0"')

        when:
        ext.validateDependencies()

        then:
        thrown(InvalidWPILibVersionException)

        where:
        oldVersion << ['2027.1.0', '2027.1.0-beta-1', '2027.1.0-rc-3']
    }

    def "old scheme in a vendordep version fails"() {
        given:
        def ext = extension()
        ext.wpilibVersion.set('2027.1-1.0')
        writeVendordep("\"wpilibVersion\": \"${oldVersion}\"")

        when:
        ext.loadFrom(tempDir)
        ext.validateDependencies()

        then:
        thrown(InvalidVendorDepVersionException)

        where:
        oldVersion << ['2027.1.0', '2027.1.0-alpha-1', '2027.1.0-beta-2', '2027.1.0-rc-3']
    }
}
