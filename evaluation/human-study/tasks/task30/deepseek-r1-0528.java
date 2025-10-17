
private int __read(boolean mayBlock) throws IOException {
    int ch;

    while (true) {
        // Handle non-blocking mode
        if (!mayBlock && super.available() == 0) {
            return WOULD_BLOCK;
        }

        if ((ch = super.read()) < 0) {
            return EOF;
        }

        ch = (ch & 0xff);

        // Process AYT response
        synchronized (__client) {
            __client._processAYTResponse();
        }

        // Handle spy stream
        __client._spyRead(ch);

        switch (__receiveState) {
            case _STATE_CR:
                if (ch == '\0') continue;
                // Fall through intentionally
            case _STATE_DATA:
                if (ch == TelnetCommand.IAC) {
                    __receiveState = _STATE_IAC;
                    continue;
                }
                if (ch == '\r') {
                    synchronized (__client) {
                        __receiveState = __client._requestedDont(TelnetOption.BINARY) 
                            ? _STATE_CR : _STATE_DATA;
                    }
                } else {
                    __receiveState = _STATE_DATA;
                }
                break;

            case _STATE_IAC:
                switch (ch) {
                    case TelnetCommand.WILL:
                        __receiveState = _STATE_WILL;
                        continue;
                    case TelnetCommand.WONT:
                        __receiveState = _STATE_WONT;
                        continue;
                    case TelnetCommand.DO:
                        __receiveState = _STATE_DO;
                        continue;
                    case TelnetCommand.DONT:
                        __receiveState = _STATE_DONT;
                        continue;
                    case TelnetCommand.SB:
                        __suboption_count = 0;
                        __receiveState = _STATE_SB;
                        continue;
                    case TelnetCommand.IAC:
                        __receiveState = _STATE_DATA;
                        break;
                    case TelnetCommand.SE:
                        __receiveState = _STATE_DATA;
                        continue;
                    default:
                        __receiveState = _STATE_DATA;
                        __client._processCommand(ch);
                        continue;
                }
                break;
                
            case _STATE_WILL:
                synchronized (__client) {
                    __client._processWill(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
                
            case _STATE_WONT:
                synchronized (__client) {
                    __client._processWont(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
                
            case _STATE_DO:
                synchronized (__client) {
                    __client._processDo(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
                
            case _STATE_DONT:
                synchronized (__client) {
                    __client._processDont(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
                
            case _STATE_SB:
                switch (ch) {
                    case TelnetCommand.IAC:
                        __receiveState = _STATE_IAC_SB;
                        continue;
                    default:
                        if (__suboption_count < __suboption.length) {
                            __suboption[__suboption_count++] = ch;
                        }
                        break;
                }
                __receiveState = _STATE_SB;
                continue;
                
            case _STATE_IAC_SB:
                switch (ch) {
                    case TelnetCommand.SE:
                        synchronized (__client) {
                            __client._processSuboption(__suboption, __suboption_count);
                            __client._flushOutputStream();
                        }
                        __receiveState = _STATE_DATA;
                        continue;
                    case TelnetCommand.IAC:
                        if (__suboption_count < __suboption.length) {
                            __suboption[__suboption_count++] = ch;
                        }
                        break;
                    default:
                        break;
                }
                __receiveState = _STATE_SB;
                continue;
        }

        break;
    }

    return ch;
}
