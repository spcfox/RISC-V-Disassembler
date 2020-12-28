# RISC-V Disassembler

Disassembler for RISC-V binary files.
This is a training project that does not implement the entire ELF file specification.
It may not work with some files.

The input and output files are passed as command line arguments.

Build:
```
javac -cp src -d out src/Main.java
```

Run:
```
java -cp out Main examples/test.elf examples/out.asm
```

Sample output:
```
00000000: <main>                addi	sp, sp, -32
00000004: 			sw	ra, 28(sp)
00000008: 			sw	s0, 24(sp)
0000000c: 			addi	s0, sp, 32
00000010: 			addi	a0, zero, 0
00000014: 			sw	a0, 4084(s0)
00000018: 			addi	a1, zero, 64
0000001c: 			sw	a1, 4080(s0)
00000020: 			sw	a0, 4076(s0)
00000024: 			addi	a0, zero, 1
00000028: 			sw	a0, 4072(s0)
0000002c: 			jal	zero, 0
00000030: 			lw	a0, 4072(s0)
00000034: 			lw	a1, 4080(s0)
00000038: 			bge	a0, a1, 0
0000003c: 			jal	zero, 0
00000040: 			lw	a0, 4072(s0)
00000044: 			mul	a0, a0, a0
00000048: 			lw	a1, 4076(s0)
0000004c: 			add	a0, a1, a0
00000050: 			sw	a0, 4076(s0)
00000054: 			jal	zero, 0
00000058: 			lw	a0, 4072(s0)
0000005c: 			addi	a0, a0, 1
00000060: 			sw	a0, 4072(s0)
00000064: 			jal	zero, 0
00000068: 			lw	a0, 4076(s0)
0000006c: 			lw	s0, 24(sp)
00000070: 			lw	ra, 28(sp)
00000074: 			addi	sp, sp, 32
00000078: 			jalr	zero, ra, 0
```
