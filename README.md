# SmartCalculator
    Project created with the support of JetBrains Academy as an introduction to Kotlin.
created by: Michał Czapiewski
email: czapiewskimk@gmail.com
github: https://github.com/czpmk
# Purpose
    The program returns results of basic calculations using integer numbers. It supports 
expression solving, (e.g. "5 * (2^n) + (-1)^3"), assignment operations (e.g. "a = 5 + n*b") 
and keeps initialized variables in memory. Those can be invoked by typing their name in 
an expression.
# Available operations:
(operation; operator)
- addition: "+"
- subtraction: "-" 
- multiplication: "*"
- division: "/"
- exponentietion: "^"
- assignment: "="
- brackets: "(", ")"
# Limitations
    Arguments maximum (minimum) size is not clearly specified. Operations on big Integer 
numbers are supported, although recommended maximum (minimum) size is: 1^18 (1^-18).
    A number preceded by a "-" symbol will only be interpreted as a negative value when
it is the first argument in an expression or follows "=" or "(" symbol. It's recommended to 
always enclose negative numbers in parentheses.
    Multiple "+" or "-" symbols are supported, and interpreted as follows: "+++...+" = "+"
"---...-" = "-" if there is an odd number of "-" symbols, and "+" if it is even.
# Commands
    Following commands can be invoked while the program is running. Please, use
those in a new line.
"/exit" - terminate the program
"/help" - display a help message