package br.faustech.cpu;

import br.faustech.bus.Bus;
import br.faustech.memory.Memory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;

import br.faustech.memory.MemoryException;
import org.junit.Test;

public class CPUTest {
    public Memory memory = new Memory(4096);
    public Bus bus = new Bus(null,memory);


    private byte[] intToByteArray(int value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(value);
        byte[] byteArray = byteBuffer.array();
        return byteArray;
    }
    @Test
    public void cpuTest() {

        new CPU(memory, bus);//Define the memory and the bus for the CPU

//        try {
//                memory.write(intToByteArray(0b00000000010100011010000000000011), 0); //send the operation lw with immediate as 5 for the memory address
//                memory.write(intToByteArray(0b00000000010000011010000010000011), 32);//send the operation lw with immediate as 5 for the memory address
//                memory.write(intToByteArray(0b00000000000100000000000100110011), 64);//add rs0 and rs1 and save the result in rs2
//                memory.write(intToByteArray(0b00010000000100000000000110110011), 96);//sub rs0 and rs1 and save the result in rs3
//                memory.write(intToByteArray(0b00000000000100000001001000110011), 128);//sll rs0 and rs1 and save the result in rs4
//                memory.write(intToByteArray(0b00000000000100000010001010110011), 160);//slt rs0 and rs1 and save the result in rs5
//                memory.write(intToByteArray(0b00000000000100000011001100110011), 192);//sltu rs0 and rs1 and save the result in rs6
//                memory.write(intToByteArray(0b00000000000100000100001110110011), 224);//xor rs0 and rs1 and save the result in rs7
//                memory.write(intToByteArray(0b00000000000100000101010000110011), 256);//srl rs0 and rs1 and save the result in rs8
//                memory.write(intToByteArray(0b01000000000100000101010010110011), 288);//sra rs0 and rs1 and save the result in rs9
//                memory.write(intToByteArray(0b00000000000100000110010100110011), 320);//or rs0 and rs1 and save the result in rs10
//                memory.write(intToByteArray(0b00000000000100000111010110110011), 352);//and rs0 and rs1 and save the result in rs11
//                memory.write(intToByteArray(0b00000000001100001001011000110111), 384);//lui save the result in rs12
//                memory.write(intToByteArray(0b00000000000000000000011010010111), 416);//auipc the result in rs13
//                memory.write(intToByteArray(0b00000000000000000000011101101111), 448);//jal the result in rs14
//                memory.write(intToByteArray(0b00000100000000000000011111100111), 480);//jalr save the result in rs15
//                memory.write(intToByteArray(0b00000000011000101000100001100011), 512); //beq
//                memory.write(intToByteArray(0b00000000000100000001100001100011), 576); //bne
//                memory.write(intToByteArray(0b00000000001000000100100001100011), 640); //blt
//                memory.write(intToByteArray(0b00000000000100000101100001100011), 704); //bge
//                memory.write(intToByteArray(0b00000000010000001110100001100011), 768); //bltu
//                memory.write(intToByteArray(0b00000000011000101111100001100011), 832); //bgeu
//                  memory.write(intToByteArray(0b00000001000000001000100000010011), 896);// addi save on r16
//                  memory.write(intToByteArray(0b00000001000000001010100010010011), 928);// slti save on r17
//                  memory.write(intToByteArray(0b00000001000000001011100100010011), 960);
//                  memory.write(intToByteArray(0b00000001000000001100100110010011), 992);
//                  memory.write(intToByteArray(0b00000001000000001110101000010011), 1024);
//                  memory.write(intToByteArray(0b00000001000000001111101010010011), 1056);
//                  memory.write(intToByteArray(0b00000000000000001001101100010011), 1088);
//                  memory.write(intToByteArray(0b00000000000100001101101110010011), 1120);
//                  memory.write(intToByteArray(0b00000000001000001101110000010011), 1152);
//                  memory.write(intToByteArray(0b00000000000000000000000001110011), 1184);
//                  memory.write(intToByteArray(0b00000000000100000000000001110011), 1216);
//                  memory.write(intToByteArray(0b00000000000000000001110011110011), 1248);
//                  memory.write(intToByteArray(0b00000000000100001010110101110011), 1280);
//                  memory.write(intToByteArray(0b00000000001000010011110111110011), 1312);
//                  memory.write(intToByteArray(0b00000000001100011101111001110011), 1344);
//                  memory.write(intToByteArray(0b00000000010000100110111011110011), 1376);
//                  memory.write(intToByteArray(0b00000000010100101111111101110011), 1408);
//            memory.write(intToByteArray(0b00000000010000011010000010000011), 1440);
//        } catch (MemoryException e) {
//            throw new RuntimeException(e);
//        }


        for (int i = 0; i < 32; i++) { //Create values inside the memory equivalent to the index+1
            String hexString = String.format("%02x", i + 1);
            // Parse the hexadecimal string to a byte array
            byte[] value = HexFormat.of().parseHex(hexString);
            try {
                memory.write(value, i); // Write directly in memory
            } catch (MemoryException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            System.out.println("Memory before execution: " + Arrays.toString(memory.read(0,32)));
        } catch (MemoryException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Registers before execution: " + Arrays.toString(CPU.getRegisters()));
            int sendOperation   = 0b00000000010100011010000000000011; // send the operation lw with immediate as 5 for the memory address
        CPU.executeInstruction(sendOperation);
            sendOperation       = 0b00000000010000011010000010000011;// send the operation lw with immediate as 5 for the memory address
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after load: " + Arrays.toString(CPU.getRegisters()));

        // R-TYPES:
            sendOperation       = 0b00000000000100000000000100110011;// add rs0 and rs1 and save the result in rs2
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after add: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00010000000100000000000110110011;// sub rs0 and rs1 and save the result in rs3
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after sub: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000001001000110011;// sll rs0 and rs1 and save the result in rs4
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after sll: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000010001010110011;// slt rs0 and rs1 and save the result in rs5
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after slt: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000011001100110011;// sltu rs0 and rs1 and save the result in rs6
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after sltu: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000100001110110011;// xor rs0 and rs1 and save the result in rs7
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after xor: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000101010000110011;// srl rs0 and rs1 and save the result in rs8
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after srl: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b01000000000100000101010010110011;// sra rs0 and rs1 and save the result in rs9
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after sra: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000110010100110011;// or rs0 and rs1 and save the result in rs10
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after or: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000111010110110011;// and rs0 and rs1 and save the result in rs11
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after and: " + Arrays.toString(CPU.getRegisters()));

        // U-TYPES:
            sendOperation       = 0b00000000001100001001011000110111;// lui save the result in rs12
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after lui: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000000000000011010010111;// auipc the result in rs13
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after aiupc: " + Arrays.toString(CPU.getRegisters()));

        // J-TYPES:
            sendOperation       = 0b00000000100000000000011101101111;//jal the result in rs14
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after jal: " + Arrays.toString(CPU.getRegisters()));
        //I-JALR-TYPES
            sendOperation       = 0b00000011101000000000011111100111;// jalr save the result in rs15
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after jalr: " + Arrays.toString(CPU.getRegisters()));

        //B-TYPES
            sendOperation       = 0b00000000011000101000100001100011;// beq set pc 8+
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after beq: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000001100001100011;// bne set pc 8+
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after bne: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000001000000100100001100011;// blt set pc 8+
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after blt: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000101100001100011;// bge set pc 8+
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after bge: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000010000001110100001100011;// bltu set pc 8+
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after bltu: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000011000101111100001100011;// bgeu set pc 8+
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after bgeu: " + Arrays.toString(CPU.getRegisters()));

        //I-IMMEDIATE-TYPES
            sendOperation       = 0b00000001000000001000100000010011;// addi save on r16
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after addi: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000001000000001010100010010011;// slti save on r17
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after slti: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000001000000001011100100010011;// sltiu save on r18
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after sltiu: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000001000000001100100110010011;// xori save on r19
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after xori: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000001000000001110101000010011;// ori save on r20
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after ori: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000001000000001111101010010011;// andi save on r21
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after andi: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000000001001101100010011;// slli save on r22
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after slli: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100001101101110010011;// srli save on r23
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after srli: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000001000001101110000010011;// srai save on r24
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after srai: " + Arrays.toString(CPU.getRegisters()));
        //I-CSR-TYPES
            sendOperation       = 0b00000000000000000000000001110011;// ecall
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after ecall: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100000000000001110011;// ebreak
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after ebreak: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000000000001110011110011;// csrrw save on r25
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after csrrw: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000000100001010110101110011;// csrrs save on r26
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after csrrs: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000001000010011110111110011;// csrrc save on r27
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after csrrc: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000001100011101111001110011;// csrrwi save on r28
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after csrrwi: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000010000100110111011110011;// csrrsi save on r29
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after csrrsi: " + Arrays.toString(CPU.getRegisters()));
            sendOperation       = 0b00000000010100101111111101110011;// csrrci save on r30
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after csrrci: " + Arrays.toString(CPU.getRegisters()));


        sendOperation       = 0b0000000001001111010001100100011;// store the value of the sum in the third memory position
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after store: " + Arrays.toString(CPU.getRegisters()));
        try {
            System.out.println("Memory after execution: " + Arrays.toString(memory.read(0,31)));
        } catch (MemoryException e) {
            throw new RuntimeException(e);
        }
    }

}

