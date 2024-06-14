from umqtt.simple import MQTTClient
from machine import Pin, UART
import ESPWebServer
import network
import xtools
import urequests
import utime

led = Pin(2, Pin.OUT, value=0)

com = UART(2, 9600, tx=17, rx=16)
com.init(9600)

TOPIC = 'ntou01057042/demo/move'

board = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 2, 1, 0, 0, 0],
    [0, 0, 0, 1, 2, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0]
]


def count_flip_pieces(board, color, row, col, direction):
    opponent = 2 if color == 1 else 1
    count = 0

    if direction == 0:
        while row > 0:
            row -= 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 1:
        while row > 0 and col < 5:
            row -= 1
            col += 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 2:
        while col < 5:
            col += 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 3:
        while row < 5 and col < 5:
            row += 1
            col += 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 4:
        while row < 5:
            row += 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 5:
        while row < 5 and col > 0:
            row += 1
            col -= 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 6:
        while col > 0:
            col -= 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break
    elif direction == 7:
        while row > 0 and col > 0:
            row -= 1
            col -= 1
            curr = board[row][col]
            if curr == color:
                return count
            if curr == opponent:
                count += 1
            else:
                break

    return 0


def is_valid_move(board, color, row, col):
    for dr in range(8):
        if count_flip_pieces(board, color, row, col, dr) > 0:
            return True
    return False


def deepcopy_2d_list(original):
    copied = []
    for sublist in original:
        copied.append(sublist)
    return copied


def flip_pieces(board, color, row, col):
    res = deepcopy_2d_list(board)
    res[row][col] = color
    for dr in range(8):
        i, j = row, col
        num = count_flip_pieces(res, color, i, j, dr)
        while (num):
            num -= 1
            if dr == 0:
                i -= 1
                res[i][j] = color
            elif dr == 1:
                i -= 1
                j += 1
                res[i][j] = color
            elif dr == 2:
                j += 1
                res[i][j] = color
            elif dr == 3:
                i += 1
                j += 1
                res[i][j] = color
            elif dr == 4:
                i += 1
                res[i][j] = color
            elif dr == 5:
                i += 1
                j -= 1
                res[i][j] = color
            elif dr == 6:
                j -= 1
                res[i][j] = color
            elif dr == 7:
                i -= 1
                j -= 1
                res[i][j] = color
    return res


def count_color_pieces(color):
    count = 0
    for row in board:
        for col in row:
            if col == color:
                count += 1
    return count


def get_board_string():
    s = ''
    for row in board:
        for col in row:
            if col == 0:
                s += '0'
            elif col == 1:
                s += '1'
            elif col == 2:
                s += '2'
    darks = str(count_color_pieces(1))
    if (len(darks) == 1):
        s += '0'
    s += darks
    lights = str(count_color_pieces(2))
    if (len(lights) == 1):
        s += '0'
    s += lights
    return s


def count_zeros():
    return sum(row.count(0) for row in board)


def callback(topic, msg):
    print("receive message:", msg.decode())
    split = msg.split()
    color = 1 if split[2].decode("utf-8") == 'b' else 2
    global board
    if is_valid_move(board, color, int(split[0]), int(split[1])):
        print('is valid move!')
        board = flip_pieces(board, color, int(split[0]lnqw), int(split[1]))
        print(board)
        print(split, color)
        board_string = get_board_string()
        print(board_string)
        com.write(board_string)
        utime.sleep(2)
        if (count_zeros() == 0):
            com.write('GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGAAAA')
        else:
            response = urequests.get('http://192.168.87.63:8080/nextmove?boardString=' + board_string[:-4])
            print(response.text)
            utime.sleep(0.5)
            if is_valid_move(board, 2, int(response.text[0]), int(response.text[1])):
                print('ai is valid move!')
                board = flip_pieces(board, 2, int(response.text[0]), int(response.text[1]))
                com.write(get_board_string())
                print(board)
            else:
                print('ai is not valid move!')
                print(board)
            pass
    else:
        print('is not valid move!')
        print(board)

sta = network.WLAN(network.STA_IF)
sta.active(True)
sta.connect("042", "01057042")

while not sta.isconnected():
    pass

print("已連接, ip為:", sta.ifconfig()[0])

ESPWebServer.begin(80)  # 啟用網站

client = MQTTClient(
    client_id=xtools.get_id(),
    server='broker.hivemq.com',
    ssl=False,
)
client.set_callback(callback)
client.connect()
client.subscribe(TOPIC)
print('start...')
led.value(1)

while True:
    client.check_msg()
