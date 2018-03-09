Ninja Plus by Fizzed
=======================================

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Overview

Modules and functionality that the Ninja Framework should/will add at some point.
Also helps provide a Maven Bill of Materials to make building projects simpler.

## Usage

Import `ninja-plus` pom in your dependency management section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fizzed</groupId>
            <artifactId>ninja-plus</artifactId>
            <version>6.2.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

In your dependencies you do not declare versions

```xml
<dependencies>
    <dependency>
        <groupId>org.ninjaframework</groupId>
        <artifactId>ninja-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fizzed</groupId>
        <artifactId>ninja-lite</artifactId>
    </dependency>
</dependencies>
```
