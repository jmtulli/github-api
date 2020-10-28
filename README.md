# GitHub API

## Trustly's Technical Challenge for Back-End

*Challenge:
Develop an API that returns the total number of lines and the total number of bytes of all the files of a given public Github repository, grouped by file extension.*

---
## Developed by: José Marcos Tulli [![LinkedIn](https://icons.iconarchive.com/icons/danleech/simple/16/linkedin-icon.png "LinkedIn")](https://www.linkedin.com/in/josetulli) [![LinkedIn](https://icons.iconarchive.com/icons/papirus-team/papirus-apps/16/github-icon.png "GitHub")](https://github.com/jmtulli)<br/>

### API basic usage
Make a GET request to the url pattern: `https://jmtulli-githubapi.herokuapp.com/{git-owner}/{git-name}`<br/>
*Where `git-owner` is the owner of the repository and `git-name` is the name of a **public** git hub repository.*
<br/>

### Expected returns
During the execution and processing of the files of the repository, a `HTTP STATUS 307` is expected.<br/>
After the result set is completed, the return should be a JSON with the quantity of lines and the size *(in bytes)* of all the files of the repository, summed up by their extensions, along with the `HTTP STATUS 200`.

### Usage example
To get the result for *this* repository make a GET request to:

```sh
https://jmtulli-githubapi.herokuapp.com/jmtulli/github-api
```
And the return is like:
```json
{"java":{"lines":2018,"size":68261.04},"gitignore":{"lines":66,"size":788.0},"xml":{"lines":170,"size":4956.16},"md":{"lines":34,"size":1638.4},"NO_EXTENSION":{"lines":620,"size":20131.84},"jar":{"lines":0,"size":5.00135936E7},"cmd":{"lines":364,"size":13209.6},"properties":{"lines":6,"size":482.0}}
```

---
