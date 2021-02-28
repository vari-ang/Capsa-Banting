# Capsa Banting

Permainan capsa umumnya dimainkan oleh empat orang. Perangkat yang dibutuhkan adalah 52 kartu tanpa Joker. Di awal permainan, ke-52 kartu ini dibagikan sampai habis. Jadi setiap pemain mendapat 13 kartu. Ada dua macam capsa yaitu: capsa susun dan capsa banting. Pada project ini hanya fokus ke capsa banting.
Kenapa dinamakan capsa banting? Sebab ketika mengeluarkan kartu, kita membantingnya. Dalam permainan capsa banting, siapa yang sudah mengeluarkan seluruh kartunya pertama kali adalah yang menang.

# Fitur
1.	Login user. Data login (username dan password) tersimpan pada database server, di mana hanya server yang dapat mengakses langsung database
2.	Sign Up user baru
3.	Room List (setelah berhasil Login)
4.	Room dapat di-create hanya melalui server saja
5.	Semua Pemain hanya boleh join ke dalam room
6.	Dalam satu room, hanya terdapat 4 pemain. Apabila sudah ada 4 orang yang masuk, orang ke-5 yang mencoba masuk tidak bisa masuk ke dalam room game
7.	Untuk memulai room game, semua pemain harus menekan tombol ‘START’. Ketika keempat pemain itu telah menekan tombol, maka keempat pemain akan mendapatkan 13 kartu secara acak (dari 52 variasi kartu remi)
8.	Ronde pertama dimulai dari pemain yang memiliki kartu 3 wajik (diamond). Untuk meletakkan kartu di board, maka disediakan sebuah tombol “PUT”. Cara meletakkan mengikuti aturan Capsa: single card, double, threess, packet, dan lain sebagainya
9.	Tombol PUT hanya bisa muncul apabila memang giliran pemain tersebut meletakkan. Tidak ada waktu/limit untuk meletakkan
10.	Urutan pemain selanjutnya ditentukan server saat awal permainan berlangsung secara random
11.	Urutan pemain berikutnya mengikuti aturan peletakkan pada pemain awal. Misalnya: pemain yang punya 3 wajik memainkan single card, maka pemain berikutnya bermain secara singlecard. Permainan berlangsung hingga semua pemain menekan tombol pass
12.	Ronde berikutnya dimulai kepada siapa pemain yang memiliki kartu tertinggi pada ronde sebelumnya. Langkah seperti pada nomor 8
13.	Permainan berakhir apabila ada satu pemain yang kartunya telah habis

# Contoh Tampilan Aplikasi
Room List  
![Room List](https://github.com/vari8/Capsa-Banting/blob/master/screenshots/Rooms.png)

Waiting Room  
![Waiting Room](https://github.com/vari8/Capsa-Banting/blob/master/screenshots/Waiting%20Room.png)

Game
![Game](https://github.com/vari8/Capsa-Banting/blob/master/screenshots/Game%20-%201.png)

![Game](https://github.com/vari8/Capsa-Banting/blob/master/screenshots/Game%20-%202.png)
