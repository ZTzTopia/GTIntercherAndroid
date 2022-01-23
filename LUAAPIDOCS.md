# Lua Documentation
Documentation for lua api used to make own cheat.

## 📜 Enumerations

## 📜 Structure

## 📜 Functions

### 📃 Memory
Read memory.

Paramenters:
- address: a address to read
- len: length of data to read

Returns: a string with the data
```lua
memRead(address, len);
```

Write memory.

Paramenters:
- address: a address to write
- data: data to write
- len: length of data to write
```lua
memWrite(address, data, len);
```

Scan all address signatures according to the pattern.

Paramenters:
- pattern: a pattern to search
- offset: offset to add to the final address
```lua
patternScan(pattern, offset);
```

Get address by symbol.

Paramenters:
- symbol: a symbol to search
```lua
getAddressFromSymbol(address);
```

Compare a address with given pattern.

Paramenters:
- address: a address to compare with pattern
- pattern: a pattern to search
```lua
compareData(address, pattern);
```

### 📃 Growtopia
To print message to console.

Paramenters:
- message: message
```lua
LogToConsole(message);
```
