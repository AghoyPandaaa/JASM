.DATA
VAR1 DW 5
VAR2 DW 10
.CODE
MOV AX, VAR1
PUSH AX
SHOW_STACK
MOV BX, VAR2
ADD AX, BX
PRINT_REG AX
LABEL1:
DEC AX
JNZ LABEL1
PRINT_REG AX
STC
PRINT_FLAG CF
CLC
PRINT_FLAG CF
OFFSET VAR1
LENGTHOF VAR1
SIZEOF VAR1
.STACK
PUSH AX
SHOW_STACK