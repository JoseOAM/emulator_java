package br.faustech.comum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ComponentThread extends Thread {

  private final byte[] address;

  private final ComponentType type;

}