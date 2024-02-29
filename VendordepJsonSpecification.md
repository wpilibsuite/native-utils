WPILib Vendor Dependency JSON File Format Specification
===

- version: 1.0

Types
---
| Typename | JSON description |
---
| JavaArtifact | `{ groupId: String, ArtifactId: String, version: String }` |
| JniArtifact | `{ groupId: String, ArtifactId: String, version: String, skipInvalidPlatforms: Boolean, isJar: Boolean, validPlatforms: String[] }` |
| CppArtifact | `{ groupId: String, ArtifactId: String, version: String, libName: String, headerClassifier: String, sharedLibrary: Boolean, skipInvalidPlatforms: Boolean, binaryPlatforms: String[] }` |

Keys
---
| Name | Type | Description | Optional |
---
| name | String | The name of the vendordep. | No. |
| version | String | The version of the vendordep. | No. |
| uuid | String | The unique identifier for the vendordep. Used to avoid replicating vendordeps in projects. | No. |
| mavenUrls | String[] | An array of urls pointing to the root of the maven repository storing the vendordep. | No. |
| extraGroupIds | String[] | | Yes. |
| jsonUrl | String | The URL that the vendordep json file can be found at. | No. |
| fileName | String | The name of the file this vendordep should be found in. | No. |
| frcYear | String | The FRC year that this project should be compatible with | No. |
| javaDependencies | JavaArtifact[] | The Java artifacts that the project exports. | No. |
| jniDependencies | JniArtifact[] | The JNI Artifacts that the project exports. | No. |
| cppDependencies | CppArtifact[] | The C++ Artifacts that the project exports. | No. |
