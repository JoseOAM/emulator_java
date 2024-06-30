package br.faustech.comum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Abstract class that serves as a base for components which monitor specific memory addresses.
 */
@Getter // Lombok annotation to generate getter methods for all fields
@AllArgsConstructor // Lombok annotation to generate a constructor for all final fields
public abstract class ComponentThread extends Thread {

  private final int[] addresses; // Array of memory addresses that this component monitors

}
