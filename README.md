# Confluence Cleaner

Rest API to remove outdated data(page versions, attachment versions, space trash).
Current version is not Removed.

## Features

- http://localhost/confluence/rest/cleaner/1.0/api/versions/all
- http://localhost/confluence/rest/cleaner/1.0/api/versions/space/{spaceKey}
- http://localhost/confluence/rest/cleaner/1.0/api/versions/page/{pageId}
- http://localhost/confluence/rest/cleaner/1.0/api/garbages/all
- http://localhost/confluence/rest/cleaner/1.0/api/garbages/space/{spaceKey}

### /versions/all

**target**
page versions, attachment versions **for All Spaces**

**QueryString**

- type: all or page or attachment. (default:all)
- endDays: 1～ (0=today, 1=yesterday) (default:0)
- limit: 1～1000 (default:1000)

**method**

- GET: Get summary.(not delete)
- DELETE: Remove target data.

like this:
http://localhost/confluence/rest/cleaner/1.0/api/versions/all?type=all&endDays=365&limit=100

### /versions/space/{spaceKey}

**target**
page versions, attachment versions **for Target Space**

**other**
same

### /versions/page/{pageId}

**target**
page versions, attachment versions **for Target Page**

**other**
same

### /garbages/all

**target**
space trash **for All Spaces**

**QueryString**

- limit: 1～100 (default:100)

**other**
same

### /garbages/space/{spaceKey}

**target**
space trash **for Target Space**

**QueryString**

- limit: 1～100 (default:100)

**other**
same

## Tested Environment

- Confluence Server: 6.4.0

## Installation
**guide**
https://confluence.atlassian.com/upm/installing-add-ons-273875715.html#InstallingMarketplaceapps-Installingbyfileupload

**jar**
target/confluence-cleaner-1.0.0-SNAPSHOT.jar

## Author

[nh321](https://github.com/nh321/)

## License

[Apache-2.0](http://opensource.org/licenses/Apache-2.0)
