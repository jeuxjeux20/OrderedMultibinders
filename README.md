<p align="center">
  <img src="/artwork/logo.svg">
</p>
<h1 align="center">OrderedMultibinders</h1>
<p align="center">
  A library to order elements of <a href="https://github.com/google/guice">Guice</a> multibinders using annotations.
</p>

### Resources
* [Documentation](https://github.com/jeuxjeux20/OrderedMultibinders/wiki)
* [Javadoc](https://jeuxjeux20.github.io/OrderedMultibinders/)

### What is this?
OrderedMultibinders is a library for, well, ordering elements multibinder elements with relative constraints (X comes before Y).

While putting bindings in the right order in your modules works, it becomes cumbersome once you use many modules, a module might come after another and break the ordering you wanted!

This library solves this problem by using an `@Order` annotation, in which you can set **what elements come before or after**.
