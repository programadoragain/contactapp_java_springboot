package com.ferdev83.contactapi.Service;

import com.ferdev83.contactapi.Entity.Contact;
import com.ferdev83.contactapi.Repository.ContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.ferdev83.contactapi.Constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ContactService {
    private ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Contact not found"));
    }

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact) {}

    public String uploadPhoto(String id, MultipartFile file) {
        Contact contact= getContact(id);
        String photoUrl= null;
        contact.setPhotoUrl(photoFunction.apply(id,file));
        contactRepository.save(contact);
        return photoUrl;
    }

    private final Function<String, String> fileExtension= filename -> Optional.of(filename)
            .filter(name -> name.contains("."))
            .map(name -> "." + name.substring(filename.lastIndexOf("." ) + 1)).orElse(".png");

    private BiFunction<String, MultipartFile, String> photoFunction= (id, image) -> {
        try {
            String filename= id + fileExtension.apply(image.getOriginalFilename());

            Path fileLocation= Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileLocation)) {
              try {
                  Files.createDirectories(fileLocation);
                  Files.copy(image.getInputStream(), fileLocation.resolve(filename), REPLACE_EXISTING);

                  return ServletUriComponentsBuilder
                          .fromCurrentContextPath()
                          .path("/contacts/image/" + filename).toUriString();
              } catch (IOException e) {
                  throw new RuntimeException(e);
              }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable save image");
        }

        return "";
    };


}
