cmake_minimum_required(VERSION 3.15)
project (DefFileGenerator)

add_executable(DefFileGenerator bindexplib.cpp main.cpp)

set_property(TARGET DefFileGenerator PROPERTY
  MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
