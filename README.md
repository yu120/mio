# mio

## 1 Netty
### 1.1 ByteBuf
**① ByteBuf数据结构**
![ByteBuf数据结构](docs/ByteBuf数据结构.png)

**② 数据读写**
- **读数据**：从 ByteBuf 中每读取一个字节, readerIndex 自增1,ByteBuf 里面总共有 writerIndex-readerIndex 个字节可读, 由此可以推论出当 readerIndex 与 writerIndex 相等的时, ByteBuf 不可读
- **写数据**：写数据是从 writerIndex 指向的部分开始写,每写一个字节,writerIndex 自增1,直到增到 capacity,这个时候,表示 ByteBuf 已经不可写了
- **最大容量**：ByteBuf 里面其实还有一个参数 maxCapacity,当向 ByteBuf 写数据的时候,如果容量不足,那么这个时候可以进行扩容,直到 capacity 扩容到 maxCapacity,超过 maxCapacity 就会报错

**get/set和read/write区别**：
- **get/set**：不会改变读写指针
- **read/write**：会改变读写指针

**③ 方法详解**
- **readerIndex()**：返回当前的读指针
- **readerIndex(int)**：设置读指针
- **writeIndex()**：返回当前的写指针
- **writeIndex(int)**：设置写指针
- **markReaderIndex()**：把当前的读指针保存起来
- **resetReaderIndex()**：把当前的读指针恢复到之前保存的值

- **writeBytes(byte[] src)**：把字节数组src里面的数据全部写到ByteBuf。src字节数组大小的长度通常小于等于writableBytes()
- **readBytes(byte[] dst)**：把ByteBuf里面的数据全部读取到dst。dst字节数组的大小通常等于readableBytes() 

- **writeByte(byte b)**：表示往ByteBuf中写一个字节。其它类似：writeBoolean()、writeChar()、writeShort()、writeInt()、writeLong()、writeFloat()、writeDouble()
- **readByte()**：表示从ByteBuf中读取一个字节。其它类似：readBoolean()、readChar()、readShort()、readInt()、readLong()、readFloat()、readDouble()

- **release()与retain()**：默认情况下,当创建完ByteBuf时,其引用为1,然后每次调用retain()方法,引用加1；调用release()方法原理是将引用计数减1,减完发现引用计数为0时,回收ByteBuf底层分配内存

- **slice()**：把原始的ByteBuf的可读部分单独截取出来成一个新的ByteBuf,然后最大长度就是原始ByteBuf的可读长度（readableBytes()）
- **duplicate()**：把整个ByteBuf都截取出来,包括所有的数据,指针信息
 **注意**：slice()与duplicate()底层内存以及引用计数与原始的ByteBuf共享。即经过slice()或者duplicate()返回的ByteBuf调用write系列方法都会影响到原始的ByteBuf
- **copy()**：直接从原始的ByteBuf中拷贝所有的信息,包括读写指针以及底层对应的数据,所以返回的ByteBuf中写数据不会影响到原始的ByteBuf