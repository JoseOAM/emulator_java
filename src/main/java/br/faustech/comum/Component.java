package br.faustech.comum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class Component {

  private final byte[] address;

  private final ComponentType type;

}
