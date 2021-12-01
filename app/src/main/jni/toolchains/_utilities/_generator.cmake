if(DEFINED _TOOLCHAIN_UTILITIES_GENERATOR)
  return()
else()
  set(_TOOLCHAIN_UTILITIES_GENERATOR 1)
endif()

macro(_generator_permitted)
  foreach(_generator ${ARGV})
    string(FIND "${CMAKE_GENERATOR}" "${_generator}" _is_generator_not_permitted)
    if(_is_generator_not_permitted)
      if(NOT _generator_list)
        set(_generator_list "${_generator}")
      else()
        set(_generator_list "${_generator_list} or ${_generator}")
      endif()
    endif()
  endforeach()

  if(_generator_list)
    message(FATAL_ERROR "Please change generator to ${_generator_list}, Current generator: ${CMAKE_GENERATOR}")
  endif()
endmacro()

macro(_generator_not_permitted)
  foreach(_generator ${ARGV})
    string(FIND "${CMAKE_GENERATOR}" "${_generator}" _is_generator_not_permitted)
    if(NOT _is_generator_not_permitted)
      if(NOT _generator_list)
        set(_generator_list "${_generator}")
      else()
        set(_generator_list "${_generator_list} or ${_generator}")
      endif()
    endif()
  endforeach()

  if(_generator_list)
    message(FATAL_ERROR "${_generator_list} generator is not permitted, Current generator: ${CMAKE_GENERATOR}")
  endif()
endmacro()