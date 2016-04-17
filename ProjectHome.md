This library is moved to sourceforget.net https://sourceforge.net/projects/peter-dwarf/?source=directory

# A Java library to read dwarf #

It is a java version of libdwarf, if you want to read out information from dwarf. please use it.

<font color='red'>!!! Please always use the latest code from SVN, rather than download the jar</font>

Usage :
```
final Vector<Dwarf> dwarfVector = DwarfLib.init(file, meoryOffset);
```

All dwarf information will be in dwarfVector

To add a JPanel to display dwarf information :
```
PeterDwarfPanel peterDwarfPanel1 = new PeterDwarfPanel();
File file = new File("/Users/peter/workspace/PeterI/kernel/kernel");
peterDwarfPanel1.init(file.getAbsolutePath());
```

Then just add peterDwarfPanel1 to any swing container.


contact : mcheung63@hotmail.com, my name is Peter

example: http://code.google.com/p/peter-dwarf/source/browse/trunk/src/com/peterdwarf/TestPeterDwarf.java

<a href='http://peter-dwarf.googlecode.com/files/a00024_a29e9ccd226cccc310768931f03571dd0_cgraph.png'><img src='http://peter-dwarf.googlecode.com/files/a00024_a29e9ccd226cccc310768931f03571dd0_cgraph.png' /></a>

<a href='http://www3.clustrmaps.com/user/41df3692'><img src='http://www3.clustrmaps.com/stats/maps-no_clusters/code.google.com-p-peter-dwarf--thumb.jpg' alt='Locations of visitors to this page' />
</a>

![http://dwarfstd.org/dwarf_logo.gif](http://dwarfstd.org/dwarf_logo.gif)

[Dwarf specification](http://dwarfstd.org/)