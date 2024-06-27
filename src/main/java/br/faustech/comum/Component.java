package br.faustech.comum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class Component {

  private final int[] addresses;
}
