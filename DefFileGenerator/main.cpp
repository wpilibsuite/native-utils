#include <fstream>
#include <iostream>
#include <string>
#include "bindexplib.h"

static char ascii_tolower(char x) {
	if (x >= 'A' && x <= 'Z')
		return x - 'A' + 'a';
	return x;
}

// strncasecmp() is not available on non-POSIX systems, so define an
// alternative function here.
static int ascii_strncasecmp(const char *LHS, const char *RHS, size_t Length) {
	for (size_t I = 0; I < Length; ++I) {
		unsigned char LHC = ascii_tolower(LHS[I]);
		unsigned char RHC = ascii_tolower(RHS[I]);
		if (LHC != RHC)
			return LHC < RHC ? -1 : 1;
	}
	return 0;
}

/// Check if this string ends with the given \p Suffix, ignoring case.
bool endswith_lower(const std::string& input, const std::string& Suffix) {
	return input.size() >= Suffix.size() &&
		ascii_strncasecmp((input.data() + input.size()) - Suffix.size(), Suffix.data(), Suffix.size()) == 0;
}

int main(int argc, char** argv) {
	if (argc < 3) {
		std::cerr << "Not enough arguments" << std::endl;
		return 1;
	}
    FILE* fout = NULL;
	fopen_s(&fout, argv[1], "w+");
	if (!fout) {
		std::cerr << "could not open output .def file: " << argv[1]
			<< "\n";
		return 1;
	}

	bindexplib deffile;

	for (auto i = 2; i < argc; i++) {
		if (endswith_lower(argv[i], ".def")) {
			if (!deffile.AddDefinitionFile(argv[i])) {
				return 1;
			}
		}
		else {
			if (!deffile.AddObjectFile(argv[i])) {
				return 1;
			}
		}
	}
	deffile.WriteFile(fout);
	fclose(fout);
	return 0;
}
