package com.mapping.main.lidar;

import java.io.IOException;
import java.io.InputStream;

public class XV11Lidar {

    private XV11LidarEventListener eventListener;
    private InputStream inputStream;

    private int data_status = 0;
    private int data_loop_index = 0;
    private int angle, dist, quality;
    private int data_4deg_index, motor_rph_low_byte, motor_rph_high_byte, data0, data2;
    private double motor_rpm;
    private boolean show_rpm = false;

    public interface XV11LidarEventListener{
        void valueUpdated(int dist, int angle, int quality);
        void rpmUpdated(double rpm);
    }

    public XV11Lidar(InputStream is, XV11LidarEventListener eventListener) {
        this.eventListener = eventListener;
        this.inputStream = is;
    }

    public void read() throws IOException {
        if (inputStream != null) {

            while (true) {
                int inByte = inputStream.read();
                switch (data_status) {
                    case 0: // no header
                        if (inByte == 0xFA) {
                            data_status = 1;
                            data_loop_index = 1;
                        }
                        break;

                    case 1: // find 2nd FA
                        if (data_loop_index == 22) { // Theres 22 bytes in each packet. Time to start over
                            if (inByte == 0xFA) {
                                data_status = 2;
                                data_loop_index = 1;
                            } else { // if not FA search again
                                data_status = 0;
                            }
                        } else {
                            data_loop_index++;
                        }
                        break;

                    case 2: // read data out
                        if (data_loop_index == 22) { // Theres 22 bytes in each packet. Time to start over
                            if (inByte == 0xFA) {
                                data_loop_index = 1;
                            } else { // if not FA search again
                                data_status = 0;
                            }
                        } else {
                            readData(inByte, data_loop_index);
                            data_loop_index++;
                        }
                        break;
                }
            }

        }
    }

    private void readData(int inByte, int data_loop_index) {
        switch (data_loop_index) {
            /**
             * Angle Data
             */
            case 1: // 4 degree index
                data_4deg_index = inByte - 0xA0;
                angle = data_4deg_index * 4;  // 1st angle in the set of 4
                break;
            case 8:
                data0 = inByte;
                angle = data_4deg_index * 4 + 1; // 2nd angle in the set
                break;
            case 12:
                data0 = inByte;
                angle = data_4deg_index * 4 + 2; // 3rd angle in the set
                break;
            case 16:
                data0 = inByte;
                angle = data_4deg_index * 4 + 3;  // 4th angle in the set
                break;

            case 2: // speed in RPH low byte
                motor_rph_low_byte = inByte;
                break;

            case 3: // speed in RPH high byte
                motor_rph_high_byte = inByte;
                motor_rpm = (double) ((motor_rph_high_byte << 8) | motor_rph_low_byte) / 64.0;
                if (angle == 0) {
                    eventListener.rpmUpdated(motor_rpm);
                }
                break;

            /**
             * Distance Data
             */
            // first half of distance data
            case 4:
                data0 = inByte;
                break;
            // second half of distance data
            case 5:
            case 9:
            case 13:
            case 17:
                if ((inByte & 0x80) >> 7 != 0) {  // check for Invalid Flag
                    dist = 0;
                } else {
                    dist = data0 | ((inByte & 0x3F) << 8);
                }
                break;

            /**
             * Quality Data
             */
            // first half of quality data
            case 6:
            case 10:
            case 18:
            case 14:
                data2 = inByte;
                break;
            // second half of quality data
            case 7:
            case 11:
            case 15:
            case 19:
                quality = (inByte << 8) | data2; // second half of quality data
                eventListener.valueUpdated(dist, angle, quality);
                break;

            default: // others do checksum
                break;
        }
    }
}
