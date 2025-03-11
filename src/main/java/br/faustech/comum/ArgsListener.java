package br.faustech.comum;

import java.io.IOException;

public interface ArgsListener {
    void onArgsSelected(String[] args) throws IOException;
}
