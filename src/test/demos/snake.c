#include <stdint.h>

#define MAX_TAIL_LENGTH             100
#define PIXEL_WIDTH                 32
#define PIXEL_HEIGTH                24
#define GRID_WIDTH                  10
#define GRID_HEIGHT                 10

/* Variable definition with specific memory location */ 
#define FRAMEBUFFER_CONTROL         ((volatile uint32_t *)0x00400000)
#define FRAMEBUFFER_START           ((volatile uint32_t *)0x00400004)
#define FRAMEBUFFER_END             4501504
#define FRAMEBUFFER_WIDTH           320
#define FRAMEBUFFER_HEIGHT          240 
#define FRAMEBUFFER_COLOR_WHITE     0xFFFFFFFF  
#define FRAMEBUFFER_COLOR_BLACK     0x00000000 
#define FRAMEBUFFER_COLOR_RED       0x00FF0000  
#define MEMORY_PLAYER_KEY           ((volatile uint32_t *)0x00151000)
#define PLAYER_POSX                 ((volatile uint32_t *)0x00151008)
#define PLAYER_POSY                 ((volatile uint32_t *)0x0015100C)
#define NEXT_POSX                   ((volatile uint32_t *)0x00151010)
#define NEXT_POSY                   ((volatile uint32_t *)0x00151014)
#define TAIL_SIZE                   ((volatile uint32_t *)0x00151018)
#define TAIL_START                  ((volatile uint32_t *)0x0015101C)
#define TAIL_END                    ((volatile uint32_t *)0x00151020)
#define FRUIT_POSX                  ((volatile uint32_t *)0x00151024)
#define FRUIT_POSY                  ((volatile uint32_t *)0x00151028)
#define ORIGINAL_DIRECTION          ((volatile uint32_t *)0x0015102C)
#define FRUIT_DEFAULT_POSX          ((volatile uint32_t *)0x00151030)
#define FRUIT_DEFAULT_POSY          ((volatile uint32_t *)0x00151034)
#define GRID                        ((volatile uint32_t (*)[GRID_WIDTH * GRID_HEIGHT])0x00155000)
#define TAIL_POS                    ((volatile uint32_t (*)[MAX_TAIL_LENGTH][2])0x00160000)
#define DIGITS                      ((volatile int (*)[5][5][3])0x00165000)

void trap_handler(void);

int main() {
    int px;
    int py;
    int key_pressed;
    int clock = 0;

    /* Setting default values */ 
    *MEMORY_PLAYER_KEY = 68;
    *ORIGINAL_DIRECTION = 68;
    *PLAYER_POSX = 2;
    *PLAYER_POSY = 0;
    *TAIL_SIZE = 2;
    *TAIL_START = 0;
    *TAIL_END = 2;
    *FRUIT_POSX = 6;
    *FRUIT_POSY = 4;
    *FRUIT_DEFAULT_POSX = 9;
    *FRUIT_DEFAULT_POSY = 0;

    for (int a = 0; a < MAX_TAIL_LENGTH; a++) {
        for (int b = 0; b < 2; b++) {
            (*TAIL_POS)[a][b] = -1;
        }
    }
    (*TAIL_POS)[0][0] = 0;
    (*TAIL_POS)[0][1] = 0;
    (*TAIL_POS)[1][0] = 1;
    (*TAIL_POS)[1][1] = 0;

    /* Setting digits grid */ 
    /* Grid reference number 4 */ 
    (*DIGITS)[0][0][0] = 1;
    (*DIGITS)[0][0][1] = 0;
    (*DIGITS)[0][0][2] = 1;
    (*DIGITS)[0][1][0] = 1;
    (*DIGITS)[0][1][1] = 0;
    (*DIGITS)[0][1][2] = 1;
    (*DIGITS)[0][2][0] = 1;
    (*DIGITS)[0][2][1] = 1;
    (*DIGITS)[0][2][2] = 1;
    (*DIGITS)[0][3][0] = 0;
    (*DIGITS)[0][3][1] = 0;
    (*DIGITS)[0][3][2] = 1;
    (*DIGITS)[0][4][0] = 0;
    (*DIGITS)[0][4][1] = 0;
    (*DIGITS)[0][4][2] = 1;

    /* Grid reference number 3 */ 
    (*DIGITS)[1][0][0] = 1;
    (*DIGITS)[1][0][1] = 1;
    (*DIGITS)[1][0][2] = 1;
    (*DIGITS)[1][1][0] = 0;
    (*DIGITS)[1][1][1] = 0;
    (*DIGITS)[1][1][2] = 1;
    (*DIGITS)[1][2][0] = 0;
    (*DIGITS)[1][2][1] = 1;
    (*DIGITS)[1][2][2] = 1;
    (*DIGITS)[1][3][0] = 0;
    (*DIGITS)[1][3][1] = 0;
    (*DIGITS)[1][3][2] = 1;
    (*DIGITS)[1][4][0] = 1;
    (*DIGITS)[1][4][1] = 1;
    (*DIGITS)[1][4][2] = 1;

    /* Grid reference number 2 */ 
    (*DIGITS)[2][0][0] = 1;
    (*DIGITS)[2][0][1] = 1;
    (*DIGITS)[2][0][2] = 1;
    (*DIGITS)[2][1][0] = 0;
    (*DIGITS)[2][1][1] = 0;
    (*DIGITS)[2][1][2] = 1;
    (*DIGITS)[2][2][0] = 1;
    (*DIGITS)[2][2][1] = 1;
    (*DIGITS)[2][2][2] = 1;
    (*DIGITS)[2][3][0] = 1;
    (*DIGITS)[2][3][1] = 0;
    (*DIGITS)[2][3][2] = 0;
    (*DIGITS)[2][4][0] = 1;
    (*DIGITS)[2][4][1] = 1;
    (*DIGITS)[2][4][2] = 1;

    /* Grid reference number 1 */ 
    (*DIGITS)[3][0][0] = 0;
    (*DIGITS)[3][0][1] = 1;
    (*DIGITS)[3][0][2] = 0;
    (*DIGITS)[3][1][0] = 1;
    (*DIGITS)[3][1][1] = 1;
    (*DIGITS)[3][1][2] = 0;
    (*DIGITS)[3][2][0] = 0;
    (*DIGITS)[3][2][1] = 1;
    (*DIGITS)[3][2][2] = 0;
    (*DIGITS)[3][3][0] = 0;
    (*DIGITS)[3][3][1] = 1;
    (*DIGITS)[3][3][2] = 0;
    (*DIGITS)[3][4][0] = 1;
    (*DIGITS)[3][4][1] = 1;
    (*DIGITS)[3][4][2] = 1;

    /* Grid reference number 0 */ 
    (*DIGITS)[4][0][0] = 0;
    (*DIGITS)[4][0][1] = 1;
    (*DIGITS)[4][0][2] = 0;
    (*DIGITS)[4][1][0] = 1;
    (*DIGITS)[4][1][1] = 1;
    (*DIGITS)[4][1][2] = 0;
    (*DIGITS)[4][2][0] = 0;
    (*DIGITS)[4][2][1] = 1;
    (*DIGITS)[4][2][2] = 0;
    (*DIGITS)[4][3][0] = 0;
    (*DIGITS)[4][3][1] = 1;
    (*DIGITS)[4][3][2] = 0;
    (*DIGITS)[4][4][0] = 1;
    (*DIGITS)[4][4][1] = 1;
    (*DIGITS)[4][4][2] = 1;

    /* 
    * Printing a countdown from 4 to 1 
    * to block the user while the program is loading 
    */ 
    for (int d = 0; d < 5; d++) {
        int base_row = 2;
        int base_col = 3;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                if (base_row + i < GRID_WIDTH && base_col + j < GRID_HEIGHT) {
                    int val = (*DIGITS)[d][i][j];
                    (*GRID)[(base_row + i) * GRID_HEIGHT + (base_col + j)] = val;
                }
            }
        }
        
        for (int j = 0; j < GRID_HEIGHT; j++) {
            for (int i = 0; i < GRID_WIDTH; i++) {
                uint32_t color;
                switch ((*GRID)[j * GRID_HEIGHT + i]) {
                    case 0: color = FRAMEBUFFER_COLOR_BLACK; break;
                    case 1: color = FRAMEBUFFER_COLOR_WHITE; break;
                    case 2: color = FRAMEBUFFER_COLOR_RED; break;
                    default: color = FRAMEBUFFER_COLOR_BLACK; break;
                }
                for (py = 0; py < PIXEL_HEIGTH && j * PIXEL_HEIGTH + py < FRAMEBUFFER_HEIGHT; py++) {
                    for (px = 0; px < PIXEL_WIDTH && i * PIXEL_WIDTH + px < FRAMEBUFFER_WIDTH; px++) {
                        int x = i * PIXEL_WIDTH + px;
                        int y = j * PIXEL_HEIGTH + py;
                        if ((y * FRAMEBUFFER_WIDTH + x) < FRAMEBUFFER_END) {
                            FRAMEBUFFER_START[y * FRAMEBUFFER_WIDTH + x] = color;
                        }
                    }
                }
            }
        }
        *FRAMEBUFFER_CONTROL = 0;
        for (int countdown = 0; countdown < 100000; countdown++) {
            *MEMORY_PLAYER_KEY = 68;
        }
    }

    for (int i = 0; i < GRID_WIDTH; i++) {
        for (int j = 0; j < GRID_HEIGHT; j++) {
            (*GRID)[j * GRID_HEIGHT + i] = 0;
        }
    }
    *FRAMEBUFFER_CONTROL = 0;

    (*GRID)[(*TAIL_POS)[0][1] * GRID_HEIGHT + (*TAIL_POS)[0][0]] = 1;
    (*GRID)[(*TAIL_POS)[1][1] * GRID_HEIGHT + (*TAIL_POS)[1][0]] = 1;

    px = *PLAYER_POSX;
    py = *PLAYER_POSY;
    (*GRID)[py * GRID_HEIGHT + px] = 1;
    (*GRID)[*FRUIT_POSY * GRID_HEIGHT + *FRUIT_POSX] = 2;

    /* Organizing interrupt handler directly on assembly code */ 
    asm volatile("csrrwi x5, 772, 0\n\t");
    asm volatile("la x7, trap_handler\n\t"
                 "csrrw x5, 773, x7\n\t");
    asm volatile("csrrwi x5, 772, 1\n\t");

    while (1) {
        clock++;

        if (clock == 10000) {
            px = *PLAYER_POSX;
            py = *PLAYER_POSY;

            key_pressed = *MEMORY_PLAYER_KEY;
            
            /* 
            * Check for player original direction
            * to block the player from going backwards
            */ 
            if (key_pressed == 87 && *ORIGINAL_DIRECTION == 83) {
                key_pressed = 83;
            } else if (key_pressed == 65 && *ORIGINAL_DIRECTION == 68) {
                key_pressed = 68;
            } else if (key_pressed == 83 && *ORIGINAL_DIRECTION == 87) {
                key_pressed = 87;
            } else if (key_pressed == 68 && *ORIGINAL_DIRECTION == 65) {
                key_pressed = 65;
            }

            /* Organize next position variables */ 
            if (key_pressed == 87 && *ORIGINAL_DIRECTION != 83) {
                *NEXT_POSX = px;
                *NEXT_POSY = py - 1;
            } else if (key_pressed == 65 && *ORIGINAL_DIRECTION != 68) {
                *NEXT_POSX = px - 1;
                *NEXT_POSY = py;
            } else if (key_pressed == 83 && *ORIGINAL_DIRECTION != 87) {
                *NEXT_POSX = px;
                *NEXT_POSY = py + 1;
            } else if (key_pressed == 68 && *ORIGINAL_DIRECTION != 65) {
                *NEXT_POSX = px + 1;
                *NEXT_POSY = py;
            } else if (key_pressed == 256) {
                asm volatile("ebreak");
            }

            *ORIGINAL_DIRECTION = key_pressed;

            int nx = *NEXT_POSX;
            int ny = *NEXT_POSY;

            if (nx >= 0 && nx < GRID_WIDTH && ny >= 0 && ny < GRID_HEIGHT) {
                if ((*GRID)[ny * GRID_HEIGHT + nx] == 1) {
                    asm volatile("ebreak");
                }
                
                /* Update tail end position with player head position */ 
                *PLAYER_POSX = nx;
                *PLAYER_POSY = ny;
                (*GRID)[ny * GRID_HEIGHT + nx] = 1;
                (*TAIL_POS)[*TAIL_END][0] = px;
                (*TAIL_POS)[*TAIL_END][1] = py;
                (*TAIL_END)++;
                if (*TAIL_END >= MAX_TAIL_LENGTH) *TAIL_END = 0;

                /* 
                * Check if the next position is a fruit
                * to handle the tail update
                * and generate new fruit if necessary
                */ 
                if (nx == *FRUIT_POSX && ny == *FRUIT_POSY) {
                    (*GRID)[ny * GRID_HEIGHT + nx] = 1;
                    if (*TAIL_SIZE < MAX_TAIL_LENGTH) (*TAIL_SIZE)++;
                    while ((*GRID)[*FRUIT_POSY * GRID_HEIGHT + *FRUIT_POSX] != 0) {
                        /* Simple calculation to generate pseudo-random fruit positions */ 
                        *FRUIT_POSX = (px + *FRUIT_POSX) * 2 + 3;
                        if (*FRUIT_POSX >= GRID_WIDTH) {
                            *FRUIT_POSX -= GRID_WIDTH;
                            if (*FRUIT_POSX >= GRID_WIDTH) {
                                *FRUIT_POSX -= GRID_WIDTH;
                            }
                        }
                        *FRUIT_POSY = (py + *FRUIT_POSY) * 2 + 2;
                        if (*FRUIT_POSY >= GRID_HEIGHT) {
                            *FRUIT_POSY -= GRID_HEIGHT;
                            if (*FRUIT_POSY >= GRID_HEIGHT) {
                                *FRUIT_POSY -= GRID_HEIGHT;
                            }
                        }

                        /* Update the fruit position with default fallback variable */ 
                        if (*FRUIT_POSX < 0 || *FRUIT_POSX >= GRID_WIDTH) *FRUIT_POSX = *FRUIT_DEFAULT_POSX;
                        if (*FRUIT_POSY < 0 || *FRUIT_POSY >= GRID_HEIGHT) *FRUIT_POSY = *FRUIT_DEFAULT_POSY;
                        (*FRUIT_DEFAULT_POSX)++;
                        (*FRUIT_DEFAULT_POSY)++;
                        if (*FRUIT_DEFAULT_POSX < 0 || *FRUIT_DEFAULT_POSX >= GRID_WIDTH) *FRUIT_DEFAULT_POSX = 0;
                        if (*FRUIT_DEFAULT_POSY < 0 || *FRUIT_DEFAULT_POSY >= GRID_WIDTH) *FRUIT_DEFAULT_POSY = 0; 
                    };
                    (*GRID)[*FRUIT_POSY * GRID_HEIGHT + *FRUIT_POSX] = 2;
                } else {
                    if ((*TAIL_POS)[*TAIL_START][0] != -1 && (*TAIL_POS)[*TAIL_START][1] != -1) {
                        (*GRID)[(*TAIL_POS)[*TAIL_START][1] * GRID_HEIGHT + (*TAIL_POS)[*TAIL_START][0]] = 0;
                    }
                    (*TAIL_START)++;
                    if (*TAIL_START >= MAX_TAIL_LENGTH) *TAIL_START = 0;
                }
            }

            /* Update the GPU grid with the correct colors */ 
            for (int j = 0; j < GRID_HEIGHT; j++) {
                for (int i = 0; i < GRID_WIDTH; i++) {
                    uint32_t color;
                    switch ((*GRID)[j * GRID_HEIGHT + i]) {
                        case 0: color = FRAMEBUFFER_COLOR_BLACK; break;
                        case 1: color = FRAMEBUFFER_COLOR_WHITE; break;
                        case 2: color = FRAMEBUFFER_COLOR_RED; break;
                        default: color = FRAMEBUFFER_COLOR_BLACK; break;
                    }
                    for (py = 0; py < PIXEL_HEIGTH && j * PIXEL_HEIGTH + py < FRAMEBUFFER_HEIGHT; py++) {
                        for (px = 0; px < PIXEL_WIDTH && i * PIXEL_WIDTH + px < FRAMEBUFFER_WIDTH; px++) {
                            int x = i * PIXEL_WIDTH + px;
                            int y = j * PIXEL_HEIGTH + py;
                            if ((y * FRAMEBUFFER_WIDTH + x) < FRAMEBUFFER_END) {
                                FRAMEBUFFER_START[y * FRAMEBUFFER_WIDTH + x] = color;
                            }
                        }
                    }
                }
            }
            *FRAMEBUFFER_CONTROL = 0;
            clock = 0;
        }
    }
}

/* Trap handler definition for user interaction */ 
void trap_handler(void) {
    asm volatile(
        "addi x2, x2, -124\n\t" 
        "sw x0, 0(x2)\n\t"
        "sw x1, 4(x2)\n\t"
        "sw x2, 8(x2)\n\t" 
        "sw x3, 12(x2)\n\t"
        "sw x4, 16(x2)\n\t"
        "sw x5, 20(x2)\n\t"
        "sw x6, 24(x2)\n\t"
        "sw x7, 28(x2)\n\t"
        "sw x8, 32(x2)\n\t"
        "sw x9, 36(x2)\n\t"
        "sw x10, 40(x2)\n\t"
        "sw x11, 44(x2)\n\t"
        "sw x12, 48(x2)\n\t"
        "sw x13, 52(x2)\n\t"
        "sw x14, 56(x2)\n\t"
        "sw x15, 60(x2)\n\t"
        "sw x16, 64(x2)\n\t"
        "sw x17, 68(x2)\n\t"
        "sw x18, 72(x2)\n\t"
        "sw x19, 76(x2)\n\t"
        "sw x20, 80(x2)\n\t"
        "sw x21, 84(x2)\n\t"
        "sw x22, 88(x2)\n\t"
        "sw x23, 92(x2)\n\t"
        "sw x24, 96(x2)\n\t"
        "sw x25, 100(x2)\n\t"
        "sw x26, 104(x2)\n\t"
        "sw x27, 108(x2)\n\t"
        "sw x28, 112(x2)\n\t"
        "sw x29, 116(x2)\n\t"
        "sw x30, 120(x2)\n\t"
        "sw x31, 124(x2)\n\t");

    int mtval;
    asm volatile("csrr %0, 835\n\t" : "=r"(mtval));

    /* Saving user key pressed on specific memory location */ 
    if (mtval == 87 || mtval == 65 || mtval == 83 || mtval == 68 || mtval == 256) {
        *MEMORY_PLAYER_KEY = mtval;
    }

    asm volatile(
        "lw x0, 0(x2)\n\t"
        "lw x1, 4(x2)\n\t"
        "lw x2, 8(x2)\n\t" 
        "lw x3, 12(x2)\n\t"
        "lw x4, 16(x2)\n\t"
        "lw x5, 20(x2)\n\t"
        "lw x6, 24(x2)\n\t"
        "lw x7, 28(x2)\n\t"
        "lw x8, 32(x2)\n\t"
        "lw x9, 36(x2)\n\t"
        "lw x10, 40(x2)\n\t"
        "lw x11, 44(x2)\n\t"
        "lw x12, 48(x2)\n\t"
        "lw x13, 52(x2)\n\t"
        "lw x14, 56(x2)\n\t"
        "lw x15, 60(x2)\n\t"
        "lw x16, 64(x2)\n\t"
        "lw x17, 68(x2)\n\t"
        "lw x19, 76(x2)\n\t"
        "lw x20, 80(x2)\n\t"
        "lw x21, 84(x2)\n\t"
        "lw x22, 88(x2)\n\t"
        "lw x23, 92(x2)\n\t"
        "lw x24, 96(x2)\n\t"
        "lw x25, 100(x2)\n\t"
        "lw x26, 104(x2)\n\t"
        "lw x27, 108(x2)\n\t"
        "lw x28, 112(x2)\n\t"
        "lw x29, 116(x2)\n\t"
        "lw x30, 120(x2)\n\t"
        "lw x31, 124(x2)\n\t"
        "mret\n\t");
}