# LongId - Smart and simple Java UUID generator

Replacement for auto-incrementing ID, especially in multi-server multi-datacenter environment.

Created in response to complexity of Snowflake and Snowizard, it's only a single class.

**Advantages:**

- SQL inserts will always be at bottom of table when used as primary key
  - ID always larger than previous
  - algorithm: currentTimeMillis & intraMilliCounter & serverId

- Thread-safe
  
- Single class

- 8byte Java 'long' result, perfect for primary key

- Single or Multiple Servers 

  - Does not require server coordination

  - Instantiate with serverId (0-4095) for 100% assurance of uniqueness

- 256,000 unique IDs per second per server

- Good for years 1970 to 2557

- longId contains timestamp -> getDate(longId) 

- longId contains serverId  -> getServerId(longId) 


**Installation**

- copy the source into your code
- or
- compile 'com.communicate:longid:1.1'

**Use Example: Single Server**
```
  LongId longId = new LongId()
  longId.getNewId();
```

**Use Example: Multi Server**
```
  int serverId = 99;
  LongId longId = new LongId(serverId)
  longId.getNewId();
```

**Disclosures:**

- Not technically a UUID generator as it's not Universal, that would require 16 instead of 8 bytes
  - Uniqueness __is guaranteed__ within a single server
  - Uniqueness __is guaranteed__ within multi-server network when distinct serverId is used

- 256,000 ID's per second is theoretical, really it's a max of 256 per millisecond, when exceeded will sleep for 1ms
  - during multi-threaded testing on quad-core machine, we rarely exceeded 256 per millisecond

- Multi-server / Multi-datacenter may result in SQL inserts not exactly at the bottom of the table, depending on delay of insertion and quantity of servers.  But it will be in the bottom pages, and almost guaranteed insert into pages that are in memory.

- If you need more than 4096 servers, you must adjust the code to decrease timeEpoch to stay within 8byte/long
