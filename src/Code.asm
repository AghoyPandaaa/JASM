.DATA
VAR1 DWORD 5
VAR2 DWORD 10
.CODE
MOV EAX, VAR1
PUSH EAX
SHOW_STACK
MOV EBX, VAR2
PRINT_REG EAX
PRINT_REG EBX
MOV EDX, 0
PRINT_REG EDX
ADD EDX, 10
PRINT_REG EDX
add edx, var2
PRINT_REG EDX
PRINT_REG DL
DEC DL
PRINT_REG DL
PRINT_REG DX
PRINT_REG EDX
