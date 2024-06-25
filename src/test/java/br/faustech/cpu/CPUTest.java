package br.faustech.cpu;

import br.faustech.bus.Bus;
import br.faustech.comum.ComponentType;
import br.faustech.memory.Memory;
import java.util.Arrays;
import java.util.HexFormat;

import br.faustech.memory.MemoryException;
import org.junit.Test;

public class CPUTest {
    public Memory memory = new Memory(4096);
    public Bus bus = new Bus(null,memory);
    @Test
    public void cpuTest() {
        new CPU(memory, bus);
        byte[] value = HexFormat.of().parseHex("0a");
        try {
            memory.write(value,5);
        } catch (MemoryException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Memory before execution: " + Arrays.toString(memory.read(0,31)));
        } catch (MemoryException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Registers before execution: " + Arrays.toString(CPU.getRegisters()));
        int sendOperation = 0b00000000010100011010000000000011; // send the operation lw with immediate as 5 for the memory address
        CPU.executeInstruction(sendOperation);
        sendOperation = 0b00000000010100011010000010000011;// send the operation lw with immediate as 5 for the memory address
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after load: " + Arrays.toString(CPU.getRegisters()));
        sendOperation = 0b00000000000100000000000100110011;// add rs1 and rs2 and save the result in rs3
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after add: " + Arrays.toString(CPU.getRegisters()));
        sendOperation = 0b00000000001001111010001100100011;// store the value of the sum in the third memory position
        CPU.executeInstruction(sendOperation);
        System.out.println("Registers after store: " + Arrays.toString(CPU.getRegisters()));
        try {
            System.out.println("Memory after execution: " + Arrays.toString(memory.read(0,31)));
        } catch (MemoryException e) {
            throw new RuntimeException(e);}
    }


}

