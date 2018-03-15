# LongId - simpler and smaller than Snowflake / Snowizard

Simple Java UUID generator.

Created in response to complexity of Snowflake and Snowizard, it's only a single class.

**Advantages:**

- SQL inserts will always be at bottom of table when used as primary key
  - ID always larger than previous
  - algorithm: currentTimeMillis & intraMillCounter & serverId

- thread-safe
  
- Single class

- 8byte Java 'long' result, perfect for primary key

- Single or Multiple Servers 

  - Does not require server coordination

  - Instantiate with serverId (0-4095) for 100% assurance of uniqueness

- 256,000 unique IDs per second per server

- Good from 1970 to 2557


**Installation**

- copy the source into your code
- or
- compile 'com.communicate:longid:1.0'

**Use Example: Prototype**
```
  int serverId = 0;  
  LongId.getNewId(serverId);
```

**Use Example: Instantiated**
```
  int serverId = 0;
  LongId idGenerator = new LongId(serverId);
  
  idGenerator.getNewId();
```

**Disadvantages:**
- It's not technically a UUID generator as it's not Universal, that would require 128bits/16bytes or more
- 256,000 ID's per second is theoretical, really it's a max of 255 per millisecond, at which point it will sleep for 1ms.
- If you need more than 4096 servers, you will need to adjust the code and lose either max_servers or timeEpoch
