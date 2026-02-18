import 'package:dot_frontend/provider/auth_provider.dart';
import 'package:dot_frontend/provider/contacts_provider.dart';
import 'package:dot_frontend/model/contact.dart';
import 'package:dot_frontend/ui/widgets/background_design.dart';
import 'package:dot_frontend/ui/widgets/custom_app_bar.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class ContactsScreen extends StatefulWidget {
  const ContactsScreen({super.key});

  @override
  State<ContactsScreen> createState() => _ContactsScreenState();
}

class _ContactsScreenState extends State<ContactsScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      if (authProvider.accessToken != null) {
        Provider.of<ContactsProvider>(context, listen: false)
            .fetchContacts(authProvider.accessToken!);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: const CustomAppBar(
        title: Text('연락처'),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.pushNamed(context, '/add_contact');
        },
        backgroundColor: const Color(0xFF6C63FF),
        child: const Icon(Icons.add, color: Colors.white),
      ),
      body: Stack(
        children: [
          const BackgroundDesign(),
          SafeArea(
            child: Column(
              children: [
                // 검색창
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: TextField(
                    style: const TextStyle(color: Colors.white),
                    onChanged: (value) {
                      Provider.of<ContactsProvider>(context, listen: false)
                          .setSearchQuery(value);
                    },
                    decoration: InputDecoration(
                      hintText: '검색',
                      hintStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
                      prefixIcon:
                          Icon(Icons.search, color: Colors.white.withOpacity(0.5)),
                      filled: true,
                      fillColor: Colors.white.withOpacity(0.1),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding:
                          const EdgeInsets.symmetric(vertical: 0, horizontal: 16),
                    ),
                  ),
                ),

                // 연락처 목록
                Expanded(
                  child: Consumer<ContactsProvider>(
                    builder: (context, contactsProvider, child) {
                      if (contactsProvider.isLoading) {
                        return const Center(
                          child: CircularProgressIndicator(color: Colors.white),
                        );
                      }

                      if (contactsProvider.errorMessage != null) {
                        return Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                contactsProvider.errorMessage!,
                                style: const TextStyle(color: Colors.white),
                              ),
                              const SizedBox(height: 16),
                              ElevatedButton(
                                onPressed: () {
                                  final authProvider = Provider.of<AuthProvider>(context, listen: false);
                                  if (authProvider.accessToken != null) {
                                    contactsProvider.fetchContacts(authProvider.accessToken!);
                                  }
                                },
                                child: const Text('다시 시도'),
                              ),
                            ],
                          ),
                        );
                      }

                      final contacts = contactsProvider.filteredContacts;

                      if (contacts.isEmpty) {
                        return Center(
                          child: Text(
                            '연락처가 없습니다.',
                            style: TextStyle(color: Colors.white.withOpacity(0.5)),
                          ),
                        );
                      }

                      return ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        itemCount: contacts.length,
                        itemBuilder: (context, index) {
                          final contact = contacts[index];
                          return _buildContactTile(context, contact);
                        },
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildContactTile(BuildContext context, Contact contact) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        leading: contact.profileImageUrl != null
            ? CircleAvatar(
                backgroundImage: NetworkImage(contact.profileImageUrl!),
              )
            : CircleAvatar(
                backgroundColor: const Color(0xFF6C63FF),
                foregroundColor: Colors.white,
                child: Text(contact.initial),
              ),
        title: Text(
          contact.name,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.w600,
          ),
        ),
        subtitle: Text(
          contact.phoneNumber,
          style: TextStyle(
            color: Colors.white.withOpacity(0.6),
            fontSize: 12,
          ),
        ),
        trailing:
            Icon(Icons.phone, color: Colors.greenAccent.withOpacity(0.8)),
        onTap: () {
          Navigator.pushNamed(context, '/contact/${contact.id}');
        },
      ),
    );
  }
}
