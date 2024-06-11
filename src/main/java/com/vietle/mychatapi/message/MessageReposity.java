package com.vietle.mychatapi.message;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReposity extends JpaRepository<Message, Long> {
}
